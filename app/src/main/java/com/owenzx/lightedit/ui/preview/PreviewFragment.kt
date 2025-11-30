package com.owenzx.lightedit.ui.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.owenzx.lightedit.R
import com.owenzx.lightedit.databinding.FragmentPreviewBinding
import com.owenzx.lightedit.ui.editor.EditorFragment

class PreviewFragment : Fragment() {

    private var _binding: FragmentPreviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 点击按钮跳转到 EditorFragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGoEdit.setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(
                    requireActivity()
                        .findViewById<View>(R.id.fragment_container_view).id,
                    EditorFragment()
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