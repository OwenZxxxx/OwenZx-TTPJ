package com.owenzx.lightedit.ui.editor

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.owenzx.lightedit.databinding.FragmentEditorBinding
import com.owenzx.lightedit.ui.editor.crop.AspectRatioMode

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

    // 模式：普通 / 裁剪 / 旋转 / 调色
    private enum class EditorMode {
        NORMAL,
        CROP,
        ROTATE,
        ADJUST
    }

    private var currentMode: EditorMode = EditorMode.NORMAL

    private lateinit var photoUri: Uri

    private var _binding: FragmentEditorBinding? = null
    private val binding get() = _binding!!

    // 裁剪模式标记
    private var inCropMode: Boolean = false

    // 旋转/翻转模式标记
    private var inRotateMode: Boolean = false

    // 进入旋转模式时的原图备份，用于“取消”还原
    private var rotateBackupBitmap: Bitmap? = null

    // 调色模式标记
    private var inAdjustMode: Boolean = false

    // 进入调色模式时的原始 Bitmap，用于实时调节和“按住对比”
    private var adjustBackupBitmap: Bitmap? = null

    // 当前亮度 [-100, 100]，默认 0
    private var currentBrightness: Int = 0

    // 当前对比度 [-50, 150]，默认 0
    private var currentContrast: Int = 0

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

    // 统一：底部某个 toolbar 从底下弹出来的动画
    private fun showToolbarWithSlideUp(target: View) {
        target.visibility = View.VISIBLE
        target.alpha = 0f
        // 先等它测量完高度再做动画
        target.post {
            target.translationY = target.height.toFloat()
            target.alpha = 0f
            target.visibility = View.VISIBLE
            target.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(220L)
                .start()
        }
    }

    // 根据模式切换 UI：
    // NORMAL：白底 + 顶部可见 + 主工具栏可见
    // 其他模式：顶部隐藏，底部对应 toolbar 白色，从黑底里弹出
    private fun updateUiForMode(mode: EditorMode) {
        currentMode = mode

        when (mode) {
            EditorMode.NORMAL -> {
                // 顶部返回 + 保存
                binding.layoutEditorHeader.visibility = View.VISIBLE

                // 整体背景白
                binding.layoutEditorRoot.setBackgroundColor(Color.WHITE)
                binding.editorCanvasContainer.setBackgroundColor(Color.WHITE)
                binding.layoutBottomPanel.setBackgroundColor(Color.WHITE)

                // 底部主工具栏显示
                binding.layoutEditorToolbar.visibility = View.VISIBLE

                // 其他 toolbar 隐藏
                binding.layoutCropControls.visibility = View.GONE
                binding.layoutRotateControls.visibility = View.GONE
                binding.layoutAdjustControls.visibility = View.GONE

                // 裁剪遮罩隐藏
                binding.cropOverlayView.visibility = View.GONE
            }

            EditorMode.CROP -> {
                binding.layoutEditorHeader.visibility = View.GONE

                // 编辑模式：背景整体暗掉
                binding.layoutEditorRoot.setBackgroundColor(Color.BLACK)
                binding.editorCanvasContainer.setBackgroundColor(Color.BLACK)
                binding.layoutBottomPanel.setBackgroundColor(Color.BLACK)

                // 主 toolbar 隐藏
                binding.layoutEditorToolbar.visibility = View.GONE

                // 只显示裁剪 toolbar（白色），并有上滑动画
                binding.layoutRotateControls.visibility = View.GONE
                binding.layoutAdjustControls.visibility = View.GONE

                // 先把背景设为白，区分黑底
                binding.layoutCropControls.setBackgroundColor(Color.WHITE)
                showToolbarWithSlideUp(binding.layoutCropControls)

                // 裁剪遮罩显示
                binding.cropOverlayView.visibility = View.VISIBLE
            }

            EditorMode.ROTATE -> {
                binding.layoutEditorHeader.visibility = View.GONE

                binding.layoutEditorRoot.setBackgroundColor(Color.BLACK)
                binding.editorCanvasContainer.setBackgroundColor(Color.BLACK)
                binding.layoutBottomPanel.setBackgroundColor(Color.BLACK)

                binding.layoutEditorToolbar.visibility = View.GONE
                binding.layoutCropControls.visibility = View.GONE
                binding.layoutAdjustControls.visibility = View.GONE

                binding.layoutRotateControls.setBackgroundColor(Color.WHITE)
                showToolbarWithSlideUp(binding.layoutRotateControls)

                binding.cropOverlayView.visibility = View.GONE
            }

            EditorMode.ADJUST -> {
                binding.layoutEditorHeader.visibility = View.GONE

                binding.layoutEditorRoot.setBackgroundColor(Color.BLACK)
                binding.editorCanvasContainer.setBackgroundColor(Color.BLACK)
                binding.layoutBottomPanel.setBackgroundColor(Color.BLACK)

                binding.layoutEditorToolbar.visibility = View.GONE
                binding.layoutCropControls.visibility = View.GONE
                binding.layoutRotateControls.visibility = View.GONE

                binding.layoutAdjustControls.setBackgroundColor(Color.WHITE)
                showToolbarWithSlideUp(binding.layoutAdjustControls)

                binding.cropOverlayView.visibility = View.GONE
            }
        }
    }

    // 接UI + 展示图片
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 标题
        binding.tvEditorTitle.text = "编辑图片"

        // 显示图片
        // 进入编辑页：允许自动适配
        binding.imageEditCanvas.setAutoInitFitEnabled(true)
        binding.imageEditCanvas.setImageURI(photoUri)

        // 等这一轮适配做完之后，就关掉开关：后续编辑不再自动 Fit
        binding.imageEditCanvas.post {
            binding.imageEditCanvas.setAutoInitFitEnabled(false)
        }

        // 让几个 toolbar 本身默认就是白背景（方便区分黑底）
        binding.layoutEditorToolbar.setBackgroundColor(Color.WHITE)
        binding.layoutCropControls.setBackgroundColor(Color.WHITE)
        binding.layoutRotateControls.setBackgroundColor(Color.WHITE)
        binding.layoutAdjustControls.setBackgroundColor(Color.WHITE)

        // 顶部返回 → 回到 album
        binding.btnEditorBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 顶部保存（先留 TODO）
        binding.btnEditorSave.setOnClickListener {
            Toast.makeText(requireContext(), "保存功能待实现", Toast.LENGTH_SHORT).show()
        }

        // 普通工具栏：裁剪
        binding.btnToolCrop.setOnClickListener {
            enterCropMode()
        }

        // 普通工具栏：旋转
        binding.btnToolRotate.setOnClickListener {
            enterRotateMode()
        }

        // 普通工具栏：调色
        binding.btnToolAdjust.setOnClickListener {
            enterAdjustMode()
        }

        binding.btnToolText.setOnClickListener {
            Toast.makeText(requireContext(), "文字功能待实现", Toast.LENGTH_SHORT).show()
        }

        // 裁剪：取消
        binding.btnCropCancel.setOnClickListener {
            exitCropMode()
        }

        // 裁剪：确认
        binding.btnCropConfirm.setOnClickListener {
            val cropRectInView = binding.cropOverlayView.getCropRect()
            val croppedBitmap = binding.imageEditCanvas.getCroppedBitmap(cropRectInView)

            if (croppedBitmap != null) {
                // 只在这一次裁剪结果上，允许重新 FitCenter
                binding.imageEditCanvas.setAutoInitFitEnabled(true)
                binding.imageEditCanvas.setImageBitmap(croppedBitmap)
                // Fit 完立刻关掉：后面所有编辑继续保留当前视角
                binding.imageEditCanvas.setAutoInitFitEnabled(false)

                exitCropMode()
            } else {
                Toast.makeText(requireContext(), "裁剪失败，请重试", Toast.LENGTH_SHORT).show()
            }
        }

        // 裁剪比例
        binding.btnRatioFree.setOnClickListener {
            binding.cropOverlayView.setAspectRatio(AspectRatioMode.FREE)
        }
        binding.btnRatio11.setOnClickListener {
            binding.cropOverlayView.setAspectRatio(AspectRatioMode.RATIO_1_1)
        }
        binding.btnRatio43.setOnClickListener {
            binding.cropOverlayView.setAspectRatio(AspectRatioMode.RATIO_4_3)
        }
        binding.btnRatio169.setOnClickListener {
            binding.cropOverlayView.setAspectRatio(AspectRatioMode.RATIO_16_9)
        }
        binding.btnRatio34.setOnClickListener {
            binding.cropOverlayView.setAspectRatio(AspectRatioMode.RATIO_3_4)
        }
        binding.btnRatio916.setOnClickListener {
            binding.cropOverlayView.setAspectRatio(AspectRatioMode.RATIO_9_16)
        }

        // 旋转 / 翻转
        binding.btnRotateLeft90.setOnClickListener {
            applyRotate(-90f)
        }
        binding.btnRotateRight90.setOnClickListener {
            applyRotate(90f)
        }
        binding.btnRotate180.setOnClickListener {
            applyRotate(180f)
        }
        binding.btnFlipHorizontal.setOnClickListener {
            applyFlip(horizontal = true)
        }
        binding.btnFlipVertical.setOnClickListener {
            applyFlip(horizontal = false)
        }

        // 旋转：取消 / 确认
        binding.btnRotateCancel.setOnClickListener {
            exitRotateMode(applyChanges = false)
        }
        binding.btnRotateConfirm.setOnClickListener {
            exitRotateMode(applyChanges = true)
        }

        // 亮度滑条：[-100, 100]
        binding.seekBrightness.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (!inAdjustMode || !fromUser) return
                currentBrightness = progress - 100
                binding.tvBrightnessValue.text = currentBrightness.toString()
                applyAdjustPreview()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 对比度滑条：[-50, 150]
        binding.seekContrast.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (!inAdjustMode || !fromUser) return
                currentContrast = progress - 50
                binding.tvContrastValue.text = currentContrast.toString()
                applyAdjustPreview()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 调色快捷：重置
        binding.btnAdjustReset.setOnClickListener {
            if (!inAdjustMode) return@setOnClickListener
            currentBrightness = 0
            currentContrast = 0
            binding.seekBrightness.progress = 100
            binding.seekContrast.progress = 50
            binding.tvBrightnessValue.text = "0"
            binding.tvContrastValue.text = "0"
            applyAdjustPreview()
        }

        // 调色快捷：亮度 -20 / +20
        binding.btnBrightnessMinus20.setOnClickListener {
            if (!inAdjustMode) return@setOnClickListener
            currentBrightness = (currentBrightness - 20).coerceIn(-100, 100)
            binding.seekBrightness.progress = currentBrightness + 100
            binding.tvBrightnessValue.text = currentBrightness.toString()
            applyAdjustPreview()
        }
        binding.btnBrightnessPlus20.setOnClickListener {
            if (!inAdjustMode) return@setOnClickListener
            currentBrightness = (currentBrightness + 20).coerceIn(-100, 100)
            binding.seekBrightness.progress = currentBrightness + 100
            binding.tvBrightnessValue.text = currentBrightness.toString()
            applyAdjustPreview()
        }

        // 调色快捷：对比度 -20 / +20
        binding.btnContrastMinus20.setOnClickListener {
            if (!inAdjustMode) return@setOnClickListener
            currentContrast = (currentContrast - 20).coerceIn(-50, 150)
            binding.seekContrast.progress = currentContrast + 50
            binding.tvContrastValue.text = currentContrast.toString()
            applyAdjustPreview()
        }
        binding.btnContrastPlus20.setOnClickListener {
            if (!inAdjustMode) return@setOnClickListener
            currentContrast = (currentContrast + 20).coerceIn(-50, 150)
            binding.seekContrast.progress = currentContrast + 50
            binding.tvContrastValue.text = currentContrast.toString()
            applyAdjustPreview()
        }

        // 调色：取消 / 确认
        binding.btnAdjustCancel.setOnClickListener {
            exitAdjustMode(applyChanges = false)
        }
        binding.btnAdjustConfirm.setOnClickListener {
            exitAdjustMode(applyChanges = true)
        }

        // 按住对比原图
        binding.btnAdjustCompare.setOnTouchListener { _, event ->
            if (!inAdjustMode || adjustBackupBitmap == null) {
                return@setOnTouchListener false
            }

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    applyAdjustPreview(brightnessOverride = 0, contrastOverride = 0)
                    true
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    applyAdjustPreview()
                    true
                }

                else -> false
            }
        }

        // 默认：普通模式（白底）
        updateUiForMode(EditorMode.NORMAL)
    }

    // ---- 模式切换 ----

    private fun enterCropMode() {
        if (inCropMode) return
        inCropMode = true

        if (inRotateMode) exitRotateMode(applyChanges = true)
        if (inAdjustMode) exitAdjustMode(applyChanges = true)

        binding.imageEditCanvas.setCropModeEnabled(true)
        updateUiForMode(EditorMode.CROP)
    }

    private fun exitCropMode() {
        if (!inCropMode) return
        inCropMode = false

        binding.imageEditCanvas.setCropModeEnabled(false)
        updateUiForMode(EditorMode.NORMAL)
    }

    private fun enterRotateMode() {
        if (inRotateMode) return

        if (inCropMode) exitCropMode()
        if (inAdjustMode) exitAdjustMode(applyChanges = true)

        val drawable = binding.imageEditCanvas.drawable as? BitmapDrawable
        val currentBitmap = drawable?.bitmap
        if (currentBitmap == null) {
            Toast.makeText(requireContext(), "没有可编辑的图片", Toast.LENGTH_SHORT).show()
            return
        }

        rotateBackupBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true)
        inRotateMode = true

        updateUiForMode(EditorMode.ROTATE)
    }

    // 对当前图片做旋转（带安全 recycle）
    private fun applyRotate(degrees: Float) {
        if (!inRotateMode) return

        val drawable = binding.imageEditCanvas.drawable as? BitmapDrawable ?: return
        val src = drawable.bitmap ?: return
        if (src.width <= 0 || src.height <= 0) return

        val matrix = Matrix().apply {
            postRotate(degrees, src.width / 2f, src.height / 2f)
        }

        val rotated = try {
            Bitmap.createBitmap(
                src, 0, 0, src.width, src.height, matrix, true
            )
        } catch (e: IllegalArgumentException) {
            Toast.makeText(requireContext(), "旋转失败", Toast.LENGTH_SHORT).show()
            return
        }

        binding.imageEditCanvas.setImageBitmap(rotated)

        if (src != rotateBackupBitmap && !src.isRecycled) {
            src.recycle()
        }
    }

    // 翻转（带安全 recycle）
    private fun applyFlip(horizontal: Boolean) {
        if (!inRotateMode) return

        val drawable = binding.imageEditCanvas.drawable as? BitmapDrawable ?: return
        val src = drawable.bitmap ?: return
        if (src.width <= 0 || src.height <= 0) return

        val matrix = Matrix().apply {
            val cx = src.width / 2f
            val cy = src.height / 2f
            if (horizontal) {
                postScale(-1f, 1f, cx, cy)
            } else {
                postScale(1f, -1f, cx, cy)
            }
        }

        val flipped = try {
            Bitmap.createBitmap(
                src, 0, 0, src.width, src.height, matrix, true
            )
        } catch (e: IllegalArgumentException) {
            Toast.makeText(requireContext(), "翻转失败", Toast.LENGTH_SHORT).show()
            return
        }

        binding.imageEditCanvas.setImageBitmap(flipped)

        if (src != rotateBackupBitmap && !src.isRecycled) {
            src.recycle()
        }
    }

    private fun exitRotateMode(applyChanges: Boolean) {
        if (!inRotateMode) return
        inRotateMode = false

        val currentDrawable = binding.imageEditCanvas.drawable as? BitmapDrawable
        val currentBitmap = currentDrawable?.bitmap

        if (!applyChanges) {
            rotateBackupBitmap?.let { backup ->
                binding.imageEditCanvas.setImageBitmap(backup)
            }
            if (currentBitmap != null &&
                currentBitmap != rotateBackupBitmap &&
                !currentBitmap.isRecycled
            ) {
                currentBitmap.recycle()
            }
        } else {
            rotateBackupBitmap?.let {
                if (!it.isRecycled) it.recycle()
            }
        }
        rotateBackupBitmap = null

        updateUiForMode(EditorMode.NORMAL)
    }

    // 进入调色模式
    private fun enterAdjustMode() {
        if (inAdjustMode) return

        if (inCropMode) exitCropMode()
        if (inRotateMode) exitRotateMode(applyChanges = true)

        val drawable = binding.imageEditCanvas.drawable as? BitmapDrawable
        val currentBitmap = drawable?.bitmap
        if (currentBitmap == null) {
            Toast.makeText(requireContext(), "没有可调节的图片", Toast.LENGTH_SHORT).show()
            return
        }

        adjustBackupBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true)

        currentBrightness = 0
        currentContrast = 0

        binding.seekBrightness.max = 200
        binding.seekBrightness.progress = 100
        binding.tvBrightnessValue.text = "0"

        binding.seekContrast.max = 200
        binding.seekContrast.progress = 50
        binding.tvContrastValue.text = "0"

        inAdjustMode = true

        updateUiForMode(EditorMode.ADJUST)
    }

    // 调色预览
    private fun applyAdjustPreview(
        brightnessOverride: Int? = null,
        contrastOverride: Int? = null
    ) {
        if (!inAdjustMode) return

        val source = adjustBackupBitmap ?: return
        val width = source.width
        val height = source.height
        if (width <= 0 || height <= 0) return

        val brightness = brightnessOverride ?: currentBrightness
        val contrast = contrastOverride ?: currentContrast

        val brightnessOffset = brightness / 100f * 255f
        val contrastFactor = (100f + contrast) / 100f

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val color = pixels[i]

            val a = color ushr 24 and 0xFF
            var r = color ushr 16 and 0xFF
            var g = color ushr 8 and 0xFF
            var b = color and 0xFF

            r = (((r - 128) * contrastFactor) + 128f + brightnessOffset).toInt()
                .coerceIn(0, 255)
            g = (((g - 128) * contrastFactor) + 128f + brightnessOffset).toInt()
                .coerceIn(0, 255)
            b = (((b - 128) * contrastFactor) + 128f + brightnessOffset).toInt()
                .coerceIn(0, 255)

            pixels[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }

        val adjustedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        adjustedBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        val oldPreview = (binding.imageEditCanvas.drawable as? BitmapDrawable)?.bitmap
        binding.imageEditCanvas.setImageBitmap(adjustedBitmap)

        if (oldPreview != null &&
            oldPreview != adjustBackupBitmap &&
            !oldPreview.isRecycled
        ) {
            oldPreview.recycle()
        }
    }

    // 退出调色模式
    private fun exitAdjustMode(applyChanges: Boolean) {
        if (!inAdjustMode) return
        inAdjustMode = false

        val currentDrawable = binding.imageEditCanvas.drawable as? BitmapDrawable
        val currentBitmap = currentDrawable?.bitmap

        if (!applyChanges) {
            adjustBackupBitmap?.let { backup ->
                binding.imageEditCanvas.setImageBitmap(backup)
            }

            if (currentBitmap != null &&
                currentBitmap != adjustBackupBitmap &&
                !currentBitmap.isRecycled
            ) {
                currentBitmap.recycle()
            }
        } else {
            adjustBackupBitmap?.let {
                if (!it.isRecycled) it.recycle()
            }
        }

        adjustBackupBitmap = null

        updateUiForMode(EditorMode.NORMAL)
    }

    override fun onDestroyView() {
        val b = _binding

        if (b != null) {
            val currentBitmap =
                (b.imageEditCanvas.drawable as? BitmapDrawable)?.bitmap

            rotateBackupBitmap?.let { bmp ->
                if (bmp != currentBitmap && !bmp.isRecycled) {
                    bmp.recycle()
                }
            }
            rotateBackupBitmap = null

            adjustBackupBitmap?.let { bmp ->
                if (bmp != currentBitmap && !bmp.isRecycled) {
                    bmp.recycle()
                }
            }
            adjustBackupBitmap = null
        } else {
            rotateBackupBitmap = null
            adjustBackupBitmap = null
        }

        _binding = null
        super.onDestroyView()
    }
}
