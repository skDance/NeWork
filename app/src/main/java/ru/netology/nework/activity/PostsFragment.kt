package ru.netology.nework.activity

import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.adapter.PostsAdapter
import ru.netology.nework.adapter.onInteractionPostListener
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Post
import ru.netology.nework.util.ConstValue
import ru.netology.nework.viewModel.AuthViewModel
import ru.netology.nework.viewModel.PostViewModel
import javax.inject.Inject


@AndroidEntryPoint
class PostsFragment : Fragment() {

    private val viewModel: PostViewModel by activityViewModels()

    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var appAuth: AppAuth

    private val mediaPlayer = MediaPlayer()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentPostsBinding.inflate(
            inflater,
            container,
            false
        )

        val adapter = PostsAdapter(object : onInteractionPostListener {
            override fun openPost(post: Post) {
                viewModel.openPostById.value = post.id
//                findNavController().navigate(R.id.action_feedFragment_to_openPostFragment)
            }

            override fun onLike(post: Post) {
                if (!authViewModel.authorized) showDialog(ConstValue.SING_IN) else viewModel.likeById(post)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, post.content)
                }

                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            override fun onRemove(postId: Long) {
                viewModel.removeById(postId)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                val text = post.content
                val bundle = Bundle()
                bundle.putString("editPostText", text)
                findNavController().navigate(
                    R.id.action_postsFragment_to_newPostFragment,
                    bundle
                )
            }

            override fun openPicture(post: Post) {
                viewModel.openPicture.value = post.attachment?.url
                findNavController().navigate(R.id.action_postsFragment_to_openImageFragment)
            }

            override fun onLink(post: Post) {
                val intent = if (post.link?.contains("https://") == true || post.link?.contains("http://") == true) {
                    Intent(Intent.ACTION_VIEW, Uri.parse(post.link))
                } else {
                    Intent(Intent.ACTION_VIEW, Uri.parse("http://${post.link}"))
                }
                startActivity(intent)
            }

            override fun onPlayAudio(post: Post) {
                if (post.attachment?.type == AttachmentType.AUDIO) {
                    if (mediaPlayer.isPlaying) {
                        mediaPlayer.pause()
                    } else {
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(post.attachment.url)
                        mediaPlayer.prepare()
                        mediaPlayer.start()
                    }
                }
            }

            override fun onPlayVideo(post: Post, videoView: VideoView?, videoFrame: FrameLayout) {
                if (post.attachment?.type == AttachmentType.VIDEO) {
                    videoView?.isVisible = true
                    val uri = Uri.parse(post.attachment.url)

                    videoView?.apply {
                        setMediaController(MediaController(requireContext()))
                        setVideoURI(uri)
                        setOnPreparedListener {
                            videoView.layoutParams?.height =
                                (resources.displayMetrics.widthPixels * (it.videoHeight.toDouble() / it.videoWidth)).toInt()
                            videoFrame.layoutParams?.height = videoView.layoutParams?.height
                            start()
                        }

                        setOnCompletionListener {

                            if (videoView.layoutParams?.width != null) {
                                videoView.layoutParams?.width = resources.displayMetrics.widthPixels
                                videoView.layoutParams?.height =
                                    (videoView.layoutParams?.width!! * 0.5625).toInt()
                                videoFrame.layoutParams?.height = videoView.layoutParams.height
                            }
                            stopPlayback()

                        }

                    }
                }
            }
        })

        binding.list.adapter = adapter
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) { viewModel.loadPosts() }
                    .show()
            }
        }

        viewModel.data.observe(viewLifecycleOwner) { data ->
            adapter.submitList(data.posts)
            binding.emptyText.isVisible = data.empty
        }

//        viewModel.newerCount.observe(viewLifecycleOwner) {
//            println("Newer count: $it")
//            if (it == 1) {
//                binding.recentEntries.isVisible = true
//            }
//        }

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        })

        val itemAnimator = binding.list.itemAnimator
        if (itemAnimator is DefaultItemAnimator) {
            itemAnimator.supportsChangeAnimations = false
        }

        binding.swipeRefresh.setOnRefreshListener { viewModel.refreshPosts() }

        binding.fab.setOnClickListener {
            if (!authViewModel.authorized) {
                showDialog(ConstValue.SING_IN)
            } else {
                findNavController().navigate(R.id.action_postsFragment_to_newPostFragment)
            }
        }

//        binding.recentEntries.setOnClickListener {
//            viewModel.showRecentEntries()
//            binding.recentEntries.isVisible = false
//        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadPosts()
            binding.swipeRefresh.isRefreshing = false
        }

        var menuProvider: MenuProvider? = null

        authViewModel.state.observe(viewLifecycleOwner) {
            menuProvider?.let { requireActivity().removeMenuProvider(it) }

            requireActivity().addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.main_menu, menu)

                    menu.setGroupVisible(R.id.authorized, authViewModel.authorized)
                    menu.setGroupVisible(R.id.unauthorized, !authViewModel.authorized)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.signout -> {
                            showDialog(ConstValue.SING_OUT)
                            true
                        }

                        R.id.signin -> {
                            findNavController().navigate(R.id.action_postsFragment_to_authorizationFragment)
                            true
                        }

                        R.id.signup -> {
                            findNavController().navigate(R.id.action_postsFragment_to_registrationFragment)
                            true
                        }

                        else -> false
                    }
                }
            }.apply { menuProvider = this }, viewLifecycleOwner)
        }

        return binding.root
    }

    fun showDialog(action: String) {
        val dialog = AlertDialog.Builder(context)
        when (action) {
            ConstValue.SING_IN -> {
                dialog
                    .setTitle(R.string.dialog_auth_title)
                    .setMessage(R.string.dialog_auth_text)
                    .setPositiveButton(R.string.dialog_positive_button) { dialog, _ ->
                        findNavController().navigate(R.id.action_postsFragment_to_authorizationFragment)
                        dialog.cancel()
                    }
                    .setNegativeButton(R.string.dialog_negative_button) { dialog, _ ->
                        dialog.cancel()
                    }
                    .create()
            }

            ConstValue.SING_OUT -> {
                dialog
                    .setTitle(R.string.dialog_logout_title)
                    .setPositiveButton(R.string.dialog_positive_button) { dialog, _ ->
                        appAuth.clear()
                        dialog.cancel()
                    }
                    .setNegativeButton(R.string.dialog_negative_button) { dialog, _ ->
                        dialog.cancel()
                    }
                    .create()
            }
        }

        dialog.show()
    }
}