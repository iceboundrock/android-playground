package li.ruoshi.playground

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import org.jetbrains.anko.onClick

class RecyclerViewDemoActivity : AppCompatActivity() {

    private lateinit var recyclerView : RecyclerView

    private val adapter : DemoAdapter = DemoAdapter()

    private var seq = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view_demo)

        recyclerView = findViewById(R.id.recycler_view) as RecyclerView

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        val addItemButton = findViewById(R.id.add_item)
        addItemButton.onClick {
            for (i in 1..100) {
                adapter.addItem(seq++)
            }
        }

        val removeItemButton = findViewById(R.id.remove_item_0)
        removeItemButton.onClick {
            adapter.removeOddItems()
        }

    }



}
