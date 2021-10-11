package com.yds.mediaplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.aliyun.player.IPlayer
import com.aliyun.player.source.UrlSource
import com.yds.aliyunplayer.widget.AliyunRenderView
import com.yds.mediaplayer.databinding.ActivityAliyunRenderViewBinding

class AliyunRenderViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityAliyunRenderViewBinding>(this,R.layout.activity_aliyun_render_view)

        //设置渲染View的类型，可选 SurfaceType.TEXTURE_VIEW 和 SurfaceType.SURFACE_VIEW
        binding.aliyunRenderView.setSurfaceType(AliyunRenderView.SurfaceType.SURFACE_VIEW)

        //
        val source = UrlSource()
        source.uri = "https://alivc-demo-cms.alicdn.com/video/videoAD.mp4"
        binding.aliyunRenderView.setDataSource(source)
        binding.aliyunRenderView.prepare()

        binding.aliyunRenderView.setOnPreparedListener { binding.aliyunRenderView.start() }

        binding.play.setOnClickListener {
            binding.aliyunRenderView.start()
        }

        binding.pause.setOnClickListener {
            binding.aliyunRenderView.pause()
        }
    }
}