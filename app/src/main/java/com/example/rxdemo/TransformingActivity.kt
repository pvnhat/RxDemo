package com.example.rxdemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.flowables.GroupedFlowable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_transforming.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Transforming is used after an Observable
 */

class TransformingActivity : AppCompatActivity() {

    private var resultLiveData = MutableLiveData<String>()
    private var bufferFlowable: Flowable<MutableList<Int>>? = null
    private var castObservable: Observable<Food>? = null
    private var concat: Flowable<String>? = null
    private var merge: Flowable<String>? = null
    private var flatMap: Flowable<String>? = null
    private var groupBy: Flowable<GroupedFlowable<String, String>>? = null
    private var mapFlowable: Flowable<String>? = null
    private var scanFlowable: Flowable<Int>? = null
    private var windowFlowable: Flowable<Flowable<Int>>? = null
    private var zipFlowable: Flowable<String>? = null
    private var concatMap: Flowable<String>? = null
    private var switchMap: Flowable<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transforming)

        initData()
        handleEvents()
        observer()
    }

    private fun observer() {
        resultLiveData.observe(this, Observer<String> {
            tv_result.text = it
        })
    }

    // range : emit one item at once if only range() , it will emit an array if this combines with buffer
    private fun initData() {

        val intOb = Flowable.just(1, 2, 3, 4)
        val strOb = Flowable.just("a", "b", "c")
        zipFlowable = Flowable.zip(intOb, strOb,
            BiFunction<Int, String, String> { num, char ->
                "zip: $num${char}"
            })

        castObservable = Observable.just(
            Pizza("num 1", 96), Pizza("num 2", 9),
            Pizza("num 3", 2)
        )
            .cast(Food::class.java)

        bufferFlowable = Flowable
            .range(0, 10)
            .buffer(4)

        // transform
        concat = Flowable.concat(
            Flowable.just("flow 1").delay(2000, TimeUnit.MILLISECONDS),
            Flowable.just("flow 2").delay(1000, TimeUnit.MILLISECONDS),
            Flowable.just("flow 3").delay(1500, TimeUnit.MILLISECONDS)
        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

        merge = Flowable.merge(
            Flowable.just("flow 1").delay(2000, TimeUnit.MILLISECONDS),
            Flowable.just("flow 2").delay(1000, TimeUnit.MILLISECONDS),
            Flowable.just("flow 3").delay(1500, TimeUnit.MILLISECONDS)
        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

        flatMap = Flowable.fromArray(1, 2, 3, 4, 5, 6)
            .flatMap { num ->
                val delay = Random().nextInt(10)
                Flowable.just("${num}z")
                    .delay(delay.toLong(), TimeUnit.SECONDS)
            }

        concatMap = Flowable.fromArray(1, 2, 3, 4, 5, 6)
            .concatMap { num ->
                val delay = Random().nextInt(10)
                Flowable.just("${num}z")
                    .delay(delay.toLong(), TimeUnit.SECONDS)
            }

        switchMap = Flowable.fromArray(1, 2, 3, 4, 5, 6)
            .switchMap { num ->
                val delay = Random().nextInt(10)
                Flowable.just("${num}z")
                    .delay(delay.toLong(), TimeUnit.SECONDS)
            }

        groupBy = Flowable.just(
            "Tiger", "Elephant", "Cat",
            "Chameleon", "Frog", "Fish", "Turtle", "Flamingo"
        )
            .groupBy { t -> t.first().toString() }

        mapFlowable = Flowable.fromArray(1, 2, 4, 5, 6)
            .map {
                val delay = Random().nextInt(5000).toLong()
                Thread.sleep(delay)
                "${it}z"
            }
            .observeOn(Schedulers.io())
            .subscribeOn(AndroidSchedulers.mainThread())

        scanFlowable = Flowable.fromArray(1, 2, 3, 4, 5, 6)
            .scan { t1, t2 ->
                // t1 is accumulated result (kq tích lũy) , t2 is new element
                t1 + t2
            }
            .observeOn(Schedulers.computation()).subscribeOn(AndroidSchedulers.mainThread())

        windowFlowable = Flowable.fromArray(1, 2, 3, 4, 5, 6, 7, 8, 9)
            .window(3, 2, 1).subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun handleEvents() {

        btn_switch_map.setOnClickListener {
            switchMap?.subscribe({
                makeLog("switchMap: $it")
            }, {
                makeLog("switchMap Err: ${it.message}")
            })
        }

        btn_concat_map.setOnClickListener {
            concatMap?.subscribe({
                makeLog("concatMap: ${it}")
            }, {
                makeLog("concatMap ERR: ${it.message}")
            })
        }

        btn_zip.setOnClickListener {
            zipFlowable?.subscribe({
                makeLog("zippes: $it")
            }, {
                makeLog("zip err: ${it.message}")
            })
        }

        val string = StringBuilder()
        tv_result.setOnClickListener {
            tv_result.text = getString(R.string.nothing)
        }

        btn_merge.setOnClickListener {
            merge?.subscribe {
                makeLog("merge: $it")
            }
        }

        btn_cast.setOnClickListener {
            string.clear()
            castObservable?.subscribe({
                makeLog(it.name)
            }, {
                it.message?.let { it1 -> makeLog(it1) }
            })
        }

        btn_buffer.setOnClickListener {
            string.clear()
            bufferFlowable?.subscribe {
                makeLog(it.toString())
            }
        }

        btn_concat.setOnClickListener {
            concat?.subscribe({
                makeLog("concat: ${it}")
            }) {
                makeLog("concat err: ${it.message}")
            }
        }

        btn_flat_map.setOnClickListener {
            flatMap?.subscribe({
                makeLog("flatMap $it")
            }, {
                makeLog("flatMap Err ${it.message}")
            })
        }

        btn_group_by.setOnClickListener {
            groupBy?.subscribe {
                it.toList().subscribe({
                    makeLog(it.toString())
                }, {
                    it.message?.let { it1 -> makeLog(it1) }
                })
            }
        }

        btn_map.setOnClickListener {
            mapFlowable?.subscribe {
                makeLog(it.toString())
            }
        }

        btn_scan.setOnClickListener {
            scanFlowable?.subscribe({
                makeLog(it.toString())
            }, {
                it.message?.let { it1 -> makeLog(it1) }
            })
        }

        btn_window.setOnClickListener {
            windowFlowable?.subscribe { flowable ->
                flowable.subscribe {
                    makeLog(it.toString())
                }
            }
        }

    }

    private fun doSomeThings(type: String): MutableList<Int> {
        val list = mutableListOf<Int>()
        for (i in 0 until 4) {
            Thread.sleep(1000)
            Log.d("cccc", "$type: $i")
            list.add(i)
        }
        return list
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, TransformingActivity::class.java)
    }

    open class Food(var name: String)

    class Pizza(name: String, size: Int) : Food(name)
}
