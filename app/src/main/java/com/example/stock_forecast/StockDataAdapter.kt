package com.example.stock_forecast


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.daily_item.view.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class StockDataAdapter(private val date: List<String>, private val prediction: List<Float>) : RecyclerView.Adapter<StockDataAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayTextView: TextView = view.dayTextView
        val priceTextView: TextView = view.priceTextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.daily_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val dates  = mutableListOf<String>()

        (date.indices).withIndex().forEach { (i) ->
            val parsedDate = LocalDate.parse(date[i], DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            dates.add(parsedDate.dayOfMonth.toString() + "." + parsedDate.monthValue.toString() +"."+ parsedDate.year.toString())
        }

        holder.dayTextView.text = dates[position]
        holder.priceTextView.text = "%.2f".format(prediction[position])
    }

    override fun getItemCount(): Int = date.size

}