package com.example.rxdemo

import android.os.Bundle
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
import kotlinx.android.synthetic.main.creating_activity.*
import org.reactivestreams.Publisher
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


/**
 * Uncomment makeLog(...) to see result from Log
 */
class CreatingObActivity : AppCompatActivity() {

    private val index = MutableLiveData<String>()
    private var disposable = CompositeDisposable()
    private var createFlowable: Flowable<String>? = null
    private var justSingle: Single<String>? = null
    private var deferFlowable: Flowable<String>? = null
    private var neverObservable: Observable<String>? = null
    private var lastName: String? = null
    private var singleRange: Flowable<Int>? = null
    private var fromIterableFlowable: Flowable<Int>? = null
    private var fromArrayFlowable: Flowable<Int>? = null
    private var callableFowable: Flowable<Int>? = null
    private var fromActionCompletable: Completable? = null
    private var runnableCompletable: Completable? = null
    private var fromFutureSingle: Single<Any>? = null
    private var timeSingle: Single<Long>? = null
    private var errorCompletable: Completable? = null
    private var repeatFlowable: Flowable<Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.creating_activity)
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
            lastName = "new just"
            justSingle?.subscribe({
                index.value = it.toString()
            }, {
                index.value = it.message
            })
        }

        btn_create.setOnClickListener {
            lastName = "new Create"
            createFlowable?.subscribe({
                //makeLog("create: $it")
            }, {
                //makeLog("create Er: ${it.message}")
            })
        }

        btn_defer.setOnClickListener {
            lastName = "new defer "
            deferFlowable?.subscribe({
                //makeLog("defer: $it")
            }, {
                // makeLog("defer Er: ${it.message}")
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
            singleRange?.subscribe {
                // makeLog(it.toString())
            }
        }

        btn_from_iterable.setOnClickListener {
            fromIterableFlowable?.subscribe({
                //makeLog("iterable $it")
            }, {
                //makeLog("iterable ${it.message}")
            })
        }

        btn_from_array.setOnClickListener {
            fromArrayFlowable?.subscribe({
                // makeLog("fromArray: $it")
            }, {
                // makeLog("fromArray Er: ${it.message}")
            })
        }

        btn_from_action.setOnClickListener {
            fromActionCompletable?.subscribe({
                //makeLog("action was done!")
            }, {
                //makeLog("err: ${it.message}")
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

        btn_next_to_creating.setOnClickListener {
            startActivity(TransformingActivity.newInstance(this))
        }

        btn_check.setOnClickListener {
            lastName = "new defer2 "
            deferFlowable?.subscribe({
                index.value = it
            }, {
                index.value = it.message
            })
        }

        btn_repeat.setOnClickListener {
            repeatFlowable?.subscribe({
                makeLog("repeat: $it")
            }, {
                makeLog("repeat Er: ${it.message}")
            })
        }

        btn_filtering.setOnClickListener {
            startActivity(FilteringObActivity.newInstance(this))
        }
    }

    private fun initData() {

        repeatFlowable = Flowable.just(1, 2).repeat(2)

        val action = Action {
            try {
                //Toast.makeText(this, "Action is running!",
                //    Toast.LENGTH_SHORT).show() //Can't toast on a thread that has not called Looper.prepare()

                doSomeThings("fromAction")
                // val c = 3/0 divide by zero
            } catch (e: Exception) {
                throw e
            }
        }

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
            //makeLog(Thread.currentThread().toString())
            doSomeThings("fromFuture")

        }, 2, TimeUnit.SECONDS)
        fromFutureSingle = Single.fromFuture(future).subscribeOn(Schedulers.io()).observeOn(
            AndroidSchedulers.mainThread()
        )

        runnableCompletable = Completable.fromRunnable(runnable)
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

        fromActionCompletable = Completable.fromAction(action)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        callableFowable = Flowable.fromCallable {
            val list = doSomeThings("callableFowable")
            list[2]
        }.subscribeOn(Schedulers.io())
            .filter(Predicate<Int> {
                return@Predicate it == 2
            }).observeOn(AndroidSchedulers.mainThread())

        // always run on thread which calls it
        val lista = listOf(1, 23, 4, 5)
        val fromFlowable = Observable.fromArray(lista)
            .subscribe {
                //makeLog("from " + it)
            }

        val justFlowable = Observable.just(lista)
            .subscribe {
                //makeLog("just $it")
            }

        val listB = listOf(1, 23, 4, 5, "saa")
        val fromIterableFlowable = Flowable.fromIterable(listB).subscribe {
            //makeLog("fromInterable " + it.toString())
        }


//        intervalFlowable = Flowable.interval(5, TimeUnit.SECONDS)
//            .subscribeOn(Schedulers.computation())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({ repeatNum ->
//                if (repeatNum == 5.toLong()) return@subscribe
//                if (repeatNum == 10.toLong()) this.intervalFlowable?.dispose()
//
//                // do something
//                makeLog("doSomething $repeatNum")
//            }, { e ->
//                makeLog("error ${e.message}")
//            })

        val justVar = Observable.fromArray(lista)
            .subscribe {
                //makeLog(it.toString())
            }

        val singleRange = Flowable.range(3, 8).subscribe {
            // makeLog(it.toString())
        }


//        justSingle = Single.just(lastName)

        createFlowable = Flowable.create(FlowableOnSubscribe<String> { emitter ->
            val task1 = doSomeThings2(1)
            if (task1 == 1) emitter.onNext("task $task1")
            val task2 = doSomeThings2(2)
            if (task2 == 2) emitter.onNext("task $task2")
            val task3 = doSomeThings2(3)
            if (task3 == 3) {
                emitter.onNext("task $task3")
                emitter.onComplete()
            }
            val task4 = doSomeThings2(4)
            if (task4 == 4) emitter.onNext("task $task4")


            if (task1 == 0 || task2 == 0 || task3 == 0) emitter.onError(Throwable("error"))

        }, BackpressureStrategy.BUFFER)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        disposable.add(RxTextView.textChanges(edt_keyword)
            .observeOn(AndroidSchedulers.mainThread())
            .filter { text -> return@filter text.length >= 3 } // filter text input
            .debounce(1000, TimeUnit.MILLISECONDS) // delay after typing in 1 seconds to avoid spam
            .subscribe { text ->
                index.postValue(text.length.toString())
            })

        lastName = "old"
        deferFlowable = Flowable.defer {
            Publisher<String> { emiter ->
                val a = doSomeThings(lastName!!)
                emiter.onNext(a.last().toString())
                emiter.onComplete()
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        // to create a delay and then do task afterward
        timeSingle = Single.timer(4, TimeUnit.SECONDS)
            .observeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        // sign an error which is available or is created through compiling
        errorCompletable = Completable.error(Exception("test error"))


    }

    private fun doSomeThings(type: String): MutableList<Int> {
        val list = mutableListOf<Int>()
        for (i in 0 until 4) {
            Thread.sleep(1000)
            //makeLog("$type $i")
            list.add(i)
        }
        return list
    }

    private fun doSomeThings2(num: Int): Int {
        Thread.sleep(2000)
        return num
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

}
