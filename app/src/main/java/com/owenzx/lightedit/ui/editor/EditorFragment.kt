package com.owenzx.lightedit.ui.editor

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.owenzx.lightedit.databinding.FragmentEditorBinding

class EditorFragment : Fragment() {

    companion object {
        private const val ARG_PHOTO_URI = "arg_photo_uri"

        fun newInstance(uri: Uri): EditorFragment {
            return EditorFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PHOTO_URI, uri)
                }
            }
        }
    }

    private lateinit var photoUri: Uri

    private var _binding: FragmentEditorBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoUri = requireArguments().getParcelable(ARG_PHOTO_URI)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 后面所有编辑相关的 UI 初始化都放在这里：
    // - 显示图片（必须）
    // - 设置手势监听
    // - 初始化裁剪框、调节滑条、工具栏按钮等
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 目前只做最小闭环：展示图片
        binding.imageEditTarget.setImageURI(photoUri)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}