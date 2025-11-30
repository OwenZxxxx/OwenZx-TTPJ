package com.owenzx.lightedit.ui.album.all

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.owenzx.lightedit.R
import com.owenzx.lightedit.databinding.FragmentAllPhotosBinding
import com.owenzx.lightedit.ui.editor.EditorFragment
import com.owenzx.lightedit.ui.preview.PreviewFragment

class AllPhotosFragment : Fragment() {

    private var _binding: FragmentAllPhotosBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AllPhotosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllPhotosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO：后面这里会换成真实图片数据
        val fakeItems = List(30) { it }  // 简单的 0..29 假数据

        adapter = AllPhotosAdapter(
            fakeItems,
            onItemClick = { position ->
                // 点击图片区域：进入编辑器
                val fm = requireActivity().supportFragmentManager
                fm.beginTransaction()
                    .replace(
                        requireActivity().findViewById<View>(R.id.fragment_container_view).id,
                        EditorFragment()
                    )
                    .addToBackStack(null)
                    .commit()
            },
            onPreviewClick = { position ->
                // 点击右下角 icon：进入预览
                // 要用的是 Activity 的 FragmentManager
                val fm = requireActivity().supportFragmentManager
                fm.beginTransaction()
                    .replace(
                        requireActivity().findViewById<View>(R.id.fragment_container_view).id,
                        PreviewFragment()
                    )
                    .addToBackStack(null)
                    .commit()
            }
        )

        val spanCount = 3  // 先搞3列，后面可以根据屏幕宽度动态算

        binding.recyclerAllPhotos.layoutManager = GridLayoutManager(requireContext(), spanCount)
        binding.recyclerAllPhotos.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}