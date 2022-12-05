package com.trill.ecommerce.util.ui

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.trill.ecommerce.R

class NumberButton : RelativeLayout {
    private lateinit var ctx: Context
    private var attrs: AttributeSet? = null
    private var styleAttr = 0
    private var mListener: OnClickListener? = null
    private var initialNumber = 0
    private var lastNumber = 0
    private var currentNumber = 0
    private var finalNumber = 0
    private var textView: TextView? = null
    private var mOnValueChangeListener: OnValueChangeListener? = null
    var addBtn: Button? = null
    var subtractBtn: Button? = null

    constructor(context: Context) : super(context) {
        this.ctx = context
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.ctx = context
        this.attrs = attrs
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.ctx = context
        this.attrs = attrs
        styleAttr = defStyleAttr
        initView()
    }

    private fun initView() {
        inflate(context, R.layout.number_button, this)
        val res = resources
        val defaultColor = res.getColor(R.color.brand_primary)
        val defaultTextColor = res.getColor(R.color.text_default)
        val defaultDrawable = res.getDrawable(R.drawable.number_button_background)
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.NumberButton,
            styleAttr, 0
        )
        initialNumber = a.getInt(R.styleable.NumberButton_initialNumber, 0)
        finalNumber = a.getInt(R.styleable.NumberButton_finalNumber, Int.MAX_VALUE)
        val textSize = a.getDimension(R.styleable.NumberButton_textSize, 13f)
        val color = a.getColor(R.styleable.NumberButton_backGroundColor, defaultColor)
        val textColor = a.getColor(R.styleable.NumberButton_textColor, defaultTextColor)
        var drawable = a.getDrawable(R.styleable.NumberButton_backgroundDrawable)
        subtractBtn = findViewById(R.id.subtract_btn)
        addBtn = findViewById(R.id.add_btn)
        textView = findViewById(R.id.number_counter)
        val mLayout = findViewById<LinearLayout>(R.id.layout)
        subtractBtn!!.setTextColor(textColor)
        addBtn!!.setTextColor(textColor)
        textView!!.setTextColor(textColor)
        subtractBtn!!.setTextSize(textSize)
        addBtn!!.setTextSize(textSize)
        textView!!.setTextSize(textSize)
        if (drawable == null) {
            drawable = defaultDrawable
        }
        assert(drawable != null)
        drawable!!.setColorFilter(PorterDuffColorFilter(color, PorterDuff.Mode.SRC))
        mLayout.background = drawable
        textView!!.setText(initialNumber.toString())
        currentNumber = initialNumber
        lastNumber = initialNumber
        subtractBtn!!.setOnClickListener(View.OnClickListener {
            val num = Integer.valueOf(textView!!.text.toString())
            setNumber((num - 1).toString(), true)
        })
        addBtn!!.setOnClickListener(View.OnClickListener {
            val num = Integer.valueOf(textView!!.getText().toString())
            setNumber((num + 1).toString(), true)
        })
        a.recycle()
    }

    private fun callListener(view: View) {
        if (mListener != null) {
            mListener!!.onClick(view)
        }
        if (mOnValueChangeListener != null) {
            if (lastNumber != currentNumber) {
                mOnValueChangeListener!!.onValueChange(this, lastNumber, currentNumber)
            }
        }
    }

    var number: String
        get() = currentNumber.toString()
        set(number) {
            lastNumber = currentNumber
            currentNumber = number.toInt()
            if (currentNumber > finalNumber) {
                currentNumber = finalNumber
            }
            if (currentNumber < initialNumber) {
                currentNumber = initialNumber
            }

            textView!!.text = currentNumber.toString()
        }

    fun setNumber(number: String, notifyListener: Boolean) {
        this.number = number
        if (notifyListener) {
            callListener(this)
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener?) {
        mListener = onClickListener
    }

    fun setOnValueChangeListener(onValueChangeListener: OnValueChangeListener?) {
        mOnValueChangeListener = onValueChangeListener
    }

    fun interface OnClickListener {
        fun onClick(view: View?)
    }

    interface OnValueChangeListener {
        fun onValueChange(view: NumberButton?, oldValue: Int, newValue: Int)
    }

    fun setRange(startingNumber: Int, endingNumber: Int) {
        initialNumber = startingNumber
        finalNumber = endingNumber
    }

    fun updateColors(backgroundColor: Int, textColor: Int) {
        textView!!.setBackgroundColor(backgroundColor)
        addBtn!!.setBackgroundColor(backgroundColor)
        subtractBtn!!.setBackgroundColor(backgroundColor)
        textView!!.setTextColor(textColor)
        addBtn!!.setTextColor(textColor)
        subtractBtn!!.setTextColor(textColor)
    }

    fun updateTextSize(unit: Int, newSize: Float) {
        textView!!.setTextSize(unit, newSize)
        addBtn!!.setTextSize(unit, newSize)
        subtractBtn!!.setTextSize(unit, newSize)
    }
}