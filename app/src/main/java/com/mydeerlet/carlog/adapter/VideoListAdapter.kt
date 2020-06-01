package com.mydeerlet.carlog.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mydeerlet.carlog.R
import com.mydeerlet.carlog.databinding.ItemVideoBinding
import com.mydeerlet.carlog.model.Video
import com.mydeerlet.carlog.view.PPImageView

/**
 * @author myDeerlet
 * @date 2020/5/30.
 * email：kuaileniaofei@163.com
 * description：
 */
class VideoListAdapter(context: Context, list: MutableList<Video>) :
    RecyclerView.Adapter<VideoListAdapter.MyViewHolder>() {

    var mContext = context
    var mList = list


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflate = LayoutInflater.from(mContext)
        val mBinding = DataBindingUtil.inflate<ItemVideoBinding>(inflate, R.layout.item_video, parent, false)
        return MyViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mList.size;
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val video = mList[position]
        holder.bindDate(video)
    }

    class MyViewHolder(mBinding: ItemVideoBinding) : RecyclerView.ViewHolder(mBinding.root) {
        val mBinding = mBinding
        fun bindDate(item: Video) {
            mBinding.video = item
            PPImageView.setImageUrl(mBinding.ivImg, item.url, false,10)
        }
    }
}