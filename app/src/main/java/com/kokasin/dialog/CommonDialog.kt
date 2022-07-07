package com.kokasin.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.kokasin.R
import com.kokasin.databinding.DialogCommonBinding

class CommonDialog(context: Context) : Dialog(context) {

    private var binding: DialogCommonBinding =
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_common, null, false)

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    fun setDialog(title: String, message: String) {
        if(TextUtils.isEmpty(title)) {
            binding.tvTitle.visibility = View.GONE
        }
        else {
            binding.tvTitle.visibility = View.VISIBLE
            binding.tvTitle.text = title
        }

        binding.tvMessage.text = message
    }

    fun setNegativeButton(buttonName: String, listener: View.OnClickListener) {
        binding.btnOk.visibility = View.GONE
        binding.btnCancel.text = buttonName
        binding.btnCancel.setOnClickListener(listener)
    }

    fun setPositiveButton(buttonName: String, listener: View.OnClickListener) {
        binding.btnOk.visibility = View.VISIBLE
        binding.btnOk.text = buttonName
        binding.btnOk.setOnClickListener(listener)
    }
}