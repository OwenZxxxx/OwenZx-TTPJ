package com.owenzx.lightedit.ui.album.all

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.owenzx.lightedit.R
import com.owenzx.lightedit.databinding.ItemPhotoGridBinding

class AllPhotosAdapter(
    private val items: List<Int>,   // 暂时用 Int 做假数据
    private val onItemClick: (position: Int) -> Unit,
    private val onPreviewClick: (position: Int) -> Unit
) : RecyclerView.Adapter<AllPhotosAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(
        // 缓存每一个 item 的 View，避免重复 findViewById
        private val binding: ItemPhotoGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            // 这里先全部用 ic_launcher 占位，后面会换成真实缩略图
            binding.imageThumbnail.setImageResource(R.mipmap.ic_launcher)

            // 点击图片区域：进入编辑
            binding.imageThumbnail.setOnClickListener {
                onItemClick(position)
            }

            // 点击右下角预览 icon：进入预览
            binding.iconPreview.setOnClickListener {
                onPreviewClick(position)
            }
        }
    }

    // 创建每个格子的布局
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPhotoGridBinding.inflate(inflater, parent, false)
        return PhotoViewHolder(binding)
    }

    // 把特定数据绑定到特定的item 滑出屏幕后复用 ViewHolder → bind 下一个位置
    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = items.size
}