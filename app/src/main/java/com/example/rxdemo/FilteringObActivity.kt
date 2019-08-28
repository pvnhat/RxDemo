package com.example.rxdemo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_filtering_ob.*
import java.util.concurrent.TimeUnit

class FilteringObActivity : AppCompatActivity() {

    private var resultLiveData = MutableLiveData<String>()
    private var debounce: Flowable<String>? = null
    private var distinct: Flowable<Int>? = null
    private var elementAt: Single<Int>? = null
    private var filter: Flowable<Int>? = null
    private var firstSingle: Single<Int>? = null
    private var ignoreCompletable: Completable? = null
    private var sample: Flowable<String>? = null
    private var take: Flowable<Int>? = null
    private var timeOut: Flowable<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_filtering_ob)

        initData()
        observer()
        handleEvents()
    }

    private fun observer() {
        resultLiveData.observe(this, Observer {
            tv_result.text = it
        })

    }

    private fun handleEvents() {
        tv_result.setOnClickListener {
            tv_result.text = getString(R.string.hello)
        }

        btn_single.setOnClickListener {
            firstSingle?.subscribe({
                makeLog("first: $it")
            }, {
                makeLog("first Err: $it")
            })
        }

        btn_debounce.setOnClickListener {
            debounce?.subscribe({
                makeLog("Debounce: $it")
            }, {
                makeLog("Debounce Er: ${it.message}")
            })
        }

        btn_distinct.setOnClickListener {
            distinct?.subscribe {
                makeLog("distinct: $it")
            }
        }

        btn_element_at.setOnClickListener {
            elementAt?.subscribe({
                makeLog("elementAt: $it")
            }, {
                makeLog("element At Err ${it}")
            })
        }

        btn_filter.setOnClickListener {
            filter?.subscribe({
                makeLog("filter: $it")
            }, {
                makeLog("filter Er: ${it.message}")
            })
        }

        btn_ignore_element.setOnClickListener {
            ignoreCompletable?.subscribe({

            }, {

            })
            ignoreCompletable?.doOnComplete {
                makeLog("ignoreCompletable: Complete!!!")
            }?.doOnTerminate {
                makeLog("ignoreCompletable: Terminate!!!")
            }?.blockingGet()
        }

        btn_sample.setOnClickListener {
            sample?.subscribe({
                makeLog("sample $it")
            }, {
                makeLog("sample Err $it")
            })
        }

        btn_take.setOnClickListener {
            take?.subscribe({
                makeLog("take $it")
            }, {
                makeLog("take Err ${it.message}")
            })
        }

        btn_time_out.setOnClickListener {
            timeOut?.subscribe({
                makeLog("timeOut $it")
            }, {
                makeLog("timeOut Err ${it.message}")
            })
        }
    }

    @SuppressLint("CheckResult")
    private fun initData() {

        val items = Flowable.just(1, 1, 2, 3,
            3, 2, 4, 5, 5)
        take = items.take(4)

        ignoreCompletable = items.ignoreElements()

        firstSingle = items.firstOrError()
        //firstMaybe = items.firstElement()


        timeOut = Flowable.create(FlowableOnSubscribe<String> { emitter ->
            emitter.onNext("A")

            Thread.sleep(250)
            emitter.onNext("B")

            Thread.sleep(600)
            emitter.onNext("C") // app will crash

            Thread.sleep(400)
            emitter.onNext("D")

            Thread.sleep(800)
            emitter.onNext("E")
            emitter.onComplete()
        }, BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .timeout(500, TimeUnit.MILLISECONDS)

        // A--B------C---D----E--------F-...
        //  -------- --1----------2------...
        // ----------C--------E----------...
        sample = Flowable.create(FlowableOnSubscribe<String> { emitter ->
            emitter.onNext("A")

            Thread.sleep(200)
            emitter.onNext("B")

            Thread.sleep(600)
            emitter.onNext("C")

            Thread.sleep(300)
            emitter.onNext("D")

            Thread.sleep(400)
            emitter.onNext("E")

            Thread.sleep(800)
            emitter.onNext("F")

            emitter.onComplete()
        }, BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .sample(1000, TimeUnit.MILLISECONDS)

        distinct = Flowable.just(
            1, 1, 2, 3, 3,
            2, 4, 5, 5
        )
            .distinctUntilChanged()

        filter = Flowable.just(
            1, 1, 2, 3, 3,
            2, 4, 5, 5
        )
            .filter { it % 2 == 0 }

        elementAt = items.elementAtOrError(15)

        debounce = Flowable.create(FlowableOnSubscribe<String> { emitter ->
            emitter.onNext("A")

            Thread.sleep(1010)
            emitter.onNext("B")

            Thread.sleep(500)
            emitter.onNext("C")

            Thread.sleep(900)
            emitter.onNext("D")

            Thread.sleep(1000)
            emitter.onNext("E")
            emitter.onComplete()
        }, BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .debounce(1000, TimeUnit.MILLISECONDS)

    }

    companion object {
        fun newInstance(context: Context) = Intent(context, FilteringObActivity::class.java)
    }
}

