package com.example.votree.tips

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.FragmentTipDetailBinding
import com.example.votree.tips.adapters.TipCommentAdapter
import com.example.votree.tips.models.ProductTip
import com.example.votree.tips.view_models.CommentViewModel
import com.example.votree.tips.view_models.TipsViewModel
import com.example.votree.users.view_models.ProfileViewModel
import com.google.android.material.button.MaterialButtonToggleGroup
import java.text.SimpleDateFormat
import java.util.Locale

class TipDetailFragment : Fragment(), MaterialButtonToggleGroup.OnButtonCheckedListener {
    private val viewModel: TipsViewModel by viewModels()
    private val commentViewModel: CommentViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val commentAdapter = TipCommentAdapter()
    private var textToSpeechHelper: TextToSpeechHelper? = null
    private var isPlaying = false
    private lateinit var binding: FragmentTipDetailBinding
    private val args : TipDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTipDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tipData = getTipData()
        setupToolbar()
        setupData(tipData)
        setupComments()

        binding.tipDetailCommentRecyclerView.adapter = commentAdapter
        binding.tipDetailCommentRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        commentViewModel.queryComments(getTipData())
        commentViewModel.commentList.observe(viewLifecycleOwner){
            Log.d("TipDetailFragment", "Comment list updated")
            commentAdapter.submitList(it)
        }

        textToSpeechHelper = TextToSpeechHelper(requireContext())

        val titleText = binding.tipDetailTitleTextView
        val authorText = binding.tipDetailAuthorTextView
        val detailContent = binding.tipDetailContentTextView
        val icSpeaker = binding.textToSpeechButton

        icSpeaker.setOnClickListener {
            if (isPlaying) {
                icSpeaker.setImageResource(R.drawable.ic_speaker_idle)
                textToSpeechHelper?.stop()
            }
            else {
                icSpeaker.setImageResource(R.drawable.ic_speaker_playing)
                textToSpeechHelper?.speak(titleText.text.toString(), authorText.text.toString(), detailContent.text.toString())
            }
            isPlaying = !isPlaying
        }
    }

    override fun onPause() {
        super.onPause()

        textToSpeechHelper?.shutdown()
        binding.tipDetailAuthorStoreLayout.setOnClickListener{
            Log.d("TipDetailFragment", "Store profile clicked")

            profileViewModel.queryUser(getTipData().userId).observe(viewLifecycleOwner){
                if (it === null || it.storeId.isEmpty()) return@observe
                val destination = TipDetailFragmentDirections.actionTipDetailFragment2ToStoreProfile2(it.storeId)
                findNavController().navigate(destination)
            }
        }
    }

    private fun setupData(tipData: ProductTip){
        binding.tipDetailTitleTextView.text = tipData.title
        binding.tipDetailContentTextView.text = tipData.content
        val sdf = SimpleDateFormat("MMM dd yyyy", Locale.ENGLISH)
        val formattedDate = tipData.createdAt?.let { sdf.format(it) + "  |  "} + tipData.vote_count.toString() + " votes"
        binding.tipDetailDateTextView.text = formattedDate

        val tipBtnGroup : MaterialButtonToggleGroup = binding.tipVotingSystem.root
        viewModel.getIsUpvoted(tipData.id).observe(viewLifecycleOwner){
            if (it !== null) {
                if (it) tipBtnGroup.check(R.id.tip_detail_upvote_btn)
                else tipBtnGroup.check(R.id.tip_detail_downvote_btn)
            }
            tipBtnGroup.addOnButtonCheckedListener(this)
        }

        Glide.with(requireContext())
            .load(tipData.imageList[0])
            .placeholder(R.drawable.img_placeholder)
            .into(binding.tipDetailImageView)

        viewModel.getAuthor(tipData.userId).observe(viewLifecycleOwner){
            if (it === null) return@observe
            binding.tipDetailAuthorTextView.text = it.fullName
            binding.tipDetailStoreTextView.text = it.storeName
            if (it.avatar.isNotBlank())
            Glide.with(requireContext())
                .load(it.avatar)
                .placeholder(R.drawable.img_placeholder)
                .into(binding.tipDetailAvatarImageView)
            else binding.tipDetailAvatarImageView.visibility = View.GONE
        }

    }

    private fun setupComments(){
        val comment = binding.tipDetailCommentEditText
        val commentBtn = binding.tipDetailSendCommentBtn
        commentBtn.setOnClickListener{
            val commentContent = comment.text.toString()
            if (commentContent.isNotBlank()){
                commentViewModel.castComment(getTipData(), commentContent)
                comment.text?.clear()
            }
            else{
                Toast.makeText(requireContext(), "Comment cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setupToolbar() {
        binding.tipDetailToolbar.inflateMenu(R.menu.tip_detail_overflow)
        binding.tipDetailToolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.tipDetailToolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.tip_detail_action_report -> {
                    val newIntent = Intent(requireContext(), TipReportActivity::class.java)

                    val tipData = getTipData()
                    newIntent.putExtra("tipData", tipData)
                    startActivity(newIntent)
                    true
                }
                else -> false
            }
        }
    }

    private fun getTipData() : ProductTip{
        return args.tipData
    }

    override fun onButtonChecked(p0: MaterialButtonToggleGroup?, p1: Int, p2: Boolean) {
        when(p1){
            R.id.tip_detail_upvote_btn -> {
                if (p2) viewModel.castVote(getTipData(), true)
                else viewModel.unVoteTip(getTipData(), true)
                Log.d("TipDetailFragment", "Upvote button clicked" + p2.toString())

            }
            R.id.tip_detail_downvote_btn -> {
                if (p2) viewModel.castVote(getTipData(), false)
                else viewModel.unVoteTip(getTipData(), false)
                Log.d("TipDetailFragment", "Downvote button clicked" + p2.toString())
            }
        }
    }
}