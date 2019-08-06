package com.example.rxdemo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Action
import io.reactivex.functions.Predicate
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val index = MutableLiveData<String>()
    private var disposable = CompositeDisposable()
    private var createSingle: Single<String>? = null
    private var justSingle: Single<Int>? = null
    private var deferSingle: Single<String>? = null
    private var neverObservable: Observable<String>? = null
    private var lastIndex: Int? = null
    private var singleRange: Flowable<Int>? = null
    private var fromIterableFlowable: Flowable<Int>? = null
    private var fromArrayFlowable: Flowable<Int>? = null
    private var callableFowable: Flowable<Int>? = null
    private var fromActionCompletable: Completable? = null
    private var runnableCompletable: Completable? = null
    private var fromFutureSingle: Single<Any>? = null
    private var timeSingle: Single<Long>? = null
    private var erroCompletable: Completable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initData()
        observer()
        handleEvent()
    }

    private fun observer() {
        index.observe(this, Observer<String> {
            tv_content.text = index.value
        })
    }

    private fun handleEvent() {
        tv_content.setOnClickListener {
            index.value = getString(R.string.hello)
        }

        // just() using thread which calls it
        btn_flowable.setOnClickListener {
            callableFowable?.subscribe({
                index.value = it.toString()
            }, {
                index.value = it.message
            })
        }

        btn_just.setOnClickListener {
            lastIndex = 1
            justSingle?.subscribe({
                index.value = it.toString()
            }, {
                index.value = it.message
            })
        }

        btn_create.setOnClickListener {
            lastIndex = 1
            createSingle?.subscribe({
                index.value = it
            }, {
                index.value = it.message
            })
        }

        btn_defer.setOnClickListener {
            lastIndex = 2
            deferSingle?.subscribe({
                index.value = it
            }, {
                index.value = it.message
            })
        }

        btn_never.setOnClickListener {
            neverObservable?.subscribe({
                index.value = "never "
            }, {
                index.value = "never err"
            })
        }

        btn_range.setOnClickListener {
            singleRange?.subscribe({
                index.value = it.toString()
            }, {
                index.value = it.message
            })
        }

        btn_from_iterable.setOnClickListener {
            fromIterableFlowable?.subscribe({
                Log.d("iterable", it.toString())
            }, {
                Log.d("iterable err", it.message)
            })
        }

        btn_from_array.setOnClickListener {
            fromArrayFlowable?.subscribe({
                Log.d("ccc array", it.toString())
            }, {
                Log.d("ccc array err", it.toString())
            })
        }

        btn_from_action.setOnClickListener {
            fromActionCompletable?.subscribe({
                index.value = "action was done!"
            }, {
                index.value = it.message
            })
        }

        btn_runnable.setOnClickListener {
            runnableCompletable?.subscribe({
                index.value = "runnable task is done!"
            }, {
                index.value = it.message
            })
        }

        btn_future.setOnClickListener {
            fromFutureSingle?.subscribe({
                index.value = "future task is done! $it"
            }, {
                index.value = it.message
            })
        }

        btn_timer.setOnClickListener {
            timeSingle?.subscribe({
                index.value = "Timer: $it"
            }, {
                index.value = it.message
            })
        }
    }

    private fun initData() {

        lastIndex = 3

        val action = Action {
            try {
                Toast.makeText(this, "Action is running!", Toast.LENGTH_SHORT).show()
                doSomeThings("fromAction")
                val c = 3 / 0
            } catch (e: Exception) {
                throw e
            }

        } // action must be not return

        val runnable = Runnable {
            try {
                Toast.makeText(this, "Runnable is running!", Toast.LENGTH_SHORT).show()
                doSomeThings("fromRunnable")
                val c = 3 / 0
            } catch (e: Exception) {
                throw e
            }
        }

        val executor = Executors.newSingleThreadScheduledExecutor()
        val future = executor.schedule({
            Log.d("cccc", "thread " + Thread.currentThread())
            doSomeThings("fromFuture")
        }, 1, TimeUnit.SECONDS)
        fromFutureSingle = Single.fromFuture(future).subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()
        )

        runnableCompletable = Completable.fromRunnable(runnable)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

        fromActionCompletable = Completable.fromAction(action).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        callableFowable = Flowable.fromCallable {
            val list = doSomeThings("callableFowable")
            list[2]
        }.subscribeOn(Schedulers.io())
            .filter(Predicate<Int> {
                return@Predicate it == 2
            }).observeOn(AndroidSchedulers.mainThread())

//        fromArrayFlowable = Flowable.fromArray(doSomeThings2(69), 2, doSomeThings2(96), 4, 4, 5, 6,
//            124, 13, 12, 3, 1, 2, 3, 4, 1,
//            1, 1, 2, 3, 3).subscribeOn(
//            Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread()) // always run on thread which calls it

//        fromIterableFlowable = Flowable.fromIterable(doSomeThings("fromIterable")).subscribeOn(
//            Schedulers.computation())
//            .observeOn(AndroidSchedulers.mainThread()) // always run on thread which calls it


//        val intervalFlowable = Flowable.interval(5, TimeUnit.SECONDS).subscribeOn(
//            Schedulers.computation()
//        ).observeOn(AndroidSchedulers.mainThread()).subscribe({
//            //  long is times subscribe
//            Toast.makeText(this, " interval doSomeThing  $it", Toast.LENGTH_SHORT).show()
//        }, {
//            Toast.makeText(this, "interval err", Toast.LENGTH_SHORT).show()
//        })

        // disposable.add(intervalFlowable)

        singleRange = Flowable.range(3, 8)
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())



        justSingle = Single.just(lastIndex)

        createSingle = Single.create<String> { emitter ->
            Single.just(lastIndex).subscribe({
                emitter.onSuccess("create $it")
            }, {
                emitter.onError(Throwable("fail"))
            })
        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

        disposable.add(RxTextView.textChanges(edt_keyword)
            .observeOn(AndroidSchedulers.mainThread())
            .filter { text -> return@filter text.length >= 3 } // filter text input
            .debounce(1000, TimeUnit.MILLISECONDS) // delay after typing in 1 seconds to avoid spam
            .subscribe { text ->
                index.postValue(text.length.toString())
            })

        deferSingle = Single.defer {
            SingleSource<String> { emitter ->
                Single.just(lastIndex).subscribe({
                    emitter.onSuccess("defer $it")
                }, {
                    emitter.onError(Throwable("fail"))
                })
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        // to create a delay and then do task afterward
        timeSingle = Single.timer(4, TimeUnit.SECONDS).observeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread())

        // sign an error which is available or is created through compiling
        erroCompletable = Completable.error(Exception("test error"))

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

    private fun doSomeThings2(num: Int): Int {
        Thread.sleep(3000)
        return num
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }
}
