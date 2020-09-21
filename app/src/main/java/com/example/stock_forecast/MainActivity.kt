package com.example.stock_forecast



import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.beust.klaxon.Klaxon
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lineChart.setNoDataText("")
        progressBar.visibility = View.INVISIBLE
    }

    class StockData(
        val name: String,
        val price: Float,
        val date: List<String>,
        val prediction: List<Float>
    )

    fun fetchJson(view: View) {
        hideKeyboard(view)
        progressBar.visibility = View.VISIBLE
        val stockName = editText.text.toString()
        val url = "http://10.0.2.2:5000/?query=%0A%7B+%0A++stockdata%28req%3A+%22$stockName%22%29+%7B%0A++++name%0A++++price%0A++++date%0A++++prediction%0A++%7D%0A%7D%0A"
        val queue = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(url, JSONObject(),
            Response.Listener { response ->
                try {
                    val data: String = response.getJSONObject("data").getJSONObject("stockdata").toString()
                    parseJson(data)
                } catch (e: JSONException) {
                    Toast.makeText(applicationContext,"No stock found. \nPlease enter stock ticker symbol e.g. \"GOOG\"", Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.INVISIBLE
                }
            },
            Response.ErrorListener { error ->
                Toast.makeText(applicationContext, "Server not responding\n$error", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.INVISIBLE
            })
        queue.add(req)
    }

    private fun parseJson(data: String){
        val stockData = Klaxon().parse<StockData>(data)
        stockData.fillTextViews()
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun StockData?.fillTextViews() {

        stocknametextView.text = this!!.name.toUpperCase()
        stockpricetextView.text = "%.3f".format(price)
        currentpricetextView.text = getString(R.string.current)
        forecastingtextView.text = getString(R.string.forecast)
        drawGraph(date, prediction)
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.adapter = StockDataAdapter(date, prediction)
        progressBar.visibility = View.INVISIBLE
    }

    private fun drawGraph( date: List<String>,  price: List<Float>){

        val entries = ArrayList<Entry>()
        val formattedDates = mutableListOf<String>()

        (0.until(date.size)).withIndex().forEach { (i) ->
            val parsedDate = LocalDate.parse(date[i], DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            formattedDates.add(parsedDate.dayOfMonth.toString() + "." + parsedDate.monthValue.toString())
        }
        (0.until(price.size)).withIndex().forEach { (i) ->
            entries.add(Entry(i.toFloat(), price[i]))
        }

        val vl = LineDataSet(entries, "Closing price prediction")
        vl.setDrawValues(false)
        vl.setDrawFilled(true)
        vl.lineWidth = 3f
        vl.fillColor = R.color.colorPrimaryDark
        vl.fillAlpha = R.color.colorAccent

        lineChart.fitScreen()
        lineChart.invalidate()
        lineChart.data = LineData(vl)
        lineChart.axisRight.isEnabled = false
        lineChart.xAxis.axisMaximum = price.size.toFloat()-0.9f
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(formattedDates)
        lineChart.setTouchEnabled(true)
        lineChart.setPinchZoom(true)
        lineChart.description.text = ""
        lineChart.animateX(1800, Easing.EaseInExpo)

        val markerView = CustomMarker(this@MainActivity, R.layout.graph_item)
        lineChart.marker = markerView
    }
    private fun hideKeyboard(view: View) {
        view.apply {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}

