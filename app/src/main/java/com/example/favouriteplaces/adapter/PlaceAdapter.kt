package com.example.favouriteplaces.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.favouriteplaces.activities.AddFavouritePlaceActivity
import com.example.favouriteplaces.activities.MainActivity
import com.example.favouriteplaces.database.FavouritePlacesDatabase
import com.example.favouriteplaces.databinding.ItemFavouritePlaceBinding
import com.example.favouriteplaces.models.FavouritePlacesModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaceAdapter(val context: Context, val list:ArrayList<FavouritePlacesModel>) :RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    private var onCLickListener:OnClickListener ?= null


    inner class ViewHolder(itemView:ItemFavouritePlaceBinding):RecyclerView.ViewHolder(itemView.root){

 //       var textViewTitle: TextView = itemView.quizTitle
//        var iconView: ImageView = itemView.quizIcon
//        var cardContainer: CardView = itemView.cardContainer

        var image : ImageView = itemView.ivPlaceImage
        var title : TextView = itemView.tvTitle
        var description : TextView = itemView.tvDescription

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
     //   return QuizViewHolder(QuizItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))

        return ViewHolder(ItemFavouritePlaceBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    fun setOnClickListener(onClickListener:OnClickListener){
        this.onCLickListener = onClickListener

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val curr = list[position]
        holder.title.text = curr.title
        holder.image.setImageURI(Uri.parse(curr.image))
        holder.description.text = curr.description

        holder.itemView.setOnClickListener{
            if(onCLickListener!=null){
                onCLickListener!!.onClick(position,curr)
            }
        }

    }

    fun removeAtad(position: Int){
        val placeDao = FavouritePlacesDatabase.getInstance(context).favouritePlacesDao()

        CoroutineScope(Dispatchers.IO).launch {
            placeDao.delete(list[position])
            list.removeAt(position)
            notifyItemRemoved(position)
        }

       // notifyDataSetChanged()


    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode:Int){
        val intent = Intent(context, AddFavouritePlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,list[position])
        activity.startActivityForResult(intent,requestCode)
        notifyItemChanged(position)

    }

    override fun getItemCount(): Int {
       return list.size
    }

    interface OnClickListener{
        fun onClick(position: Int,model: FavouritePlacesModel)
    }
}
//


//
//package com.example.quizapplication.adapters
//
//import android.content.Context
//import android.content.Intent
//import android.graphics.Color
//import android.text.Layout
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.cardview.widget.CardView
//import androidx.recyclerview.widget.RecyclerView
//import com.example.quizapplication.activities.QuestionsActivity
//import com.example.quizapplication.databinding.QuizItemBinding
//import com.example.quizapplication.models.Quiz
//import com.example.quizapplication.utils.ColorPicker
//import com.example.quizapplication.utils.IconPicker
//
//
//class QuizAdapter(val context: Context, val quizzes: List<Quiz>) :
//    RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {
//
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
//
//
//        return QuizViewHolder(QuizItemBinding.inflate(LayoutInflater.from(parent.context),parent,false))
//    }
//
//    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
//        holder.textViewTitle.text = quizzes[position].title
//        holder.cardContainer.setCardBackgroundColor(Color.parseColor(ColorPicker.getColor()))
//        holder.iconView.setImageResource(IconPicker.getIcon())
//        holder.itemView.setOnClickListener {
//            Toast.makeText(context, quizzes[position].title, Toast.LENGTH_SHORT).show()
//            val intent = Intent(context, QuestionsActivity::class.java)
//            intent.putExtra("Subject", quizzes[position].title)
//            context.startActivity(intent)
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return quizzes.size
//    }
//
//    inner class QuizViewHolder(itemView: QuizItemBinding) : RecyclerView.ViewHolder(itemView.root) {
//        var textViewTitle: TextView = itemView.quizTitle
//        var iconView: ImageView = itemView.quizIcon
//        var cardContainer: CardView = itemView.cardContainer
//    }
//}
//
//© 2022 GitHub, Inc.
//Terms
//Privacy
//Security
//Status
//Docs
//Contact GitHub
//Pricing
//API
//Training
//Blog
//About
//Loading complete