package com.owenzx.lightedit.ui.preview

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.owenzx.lightedit.R
import com.owenzx.lightedit.databinding.FragmentPreviewBinding
import com.owenzx.lightedit.ui.editor.EditorFragment

class PreviewFragment : Fragment() {

    // 通过 arguments Bundle 传参数
    // 配合 Fragment 重建（旋转屏幕 / 进程被杀再恢复）都能自动恢复
    // 没有对 Activity / View 的强引用，也不会造成泄露
    companion object {
        private const val ARG_PHOTO_URI = "arg_photo_uri"

        // 通过newInstance把uri传进来
        fun newInstance(uri: Uri): PreviewFragment {
            return PreviewFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_PHOTO_URI, uri)
                }
            }
        }
    }

    private lateinit var photoUri: Uri
    private var _binding: FragmentPreviewBinding? = null
    private val binding get() = _binding!!

    // 在 onCreate() 里读取 arguments 里的 Uri
    // 放 onCreate数据（Uri）不依赖 View，即便 Fragment 的 View 多次销毁/重建（切换 Tab 之类），photoUri 仍然可用
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 从 arguments 中取出 Uri。若没传会抛异常，说明调用方没按规范来。
        photoUri = requireArguments().getParcelable(ARG_PHOTO_URI)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 显示图片 + 点击按钮跳转到 EditorFragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 显示这张图片（后面可以加 Glide / 手写缩略图优化等)
        binding.imagePreview.setImageURI(photoUri)

        // 2. 点击按钮：把同一张 Uri 丢给 EditorFragment
        binding.btnGoEdit.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_container_view,
                    EditorFragment.newInstance(photoUri) // 把同一张 Uri 传给编辑页
                )
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}