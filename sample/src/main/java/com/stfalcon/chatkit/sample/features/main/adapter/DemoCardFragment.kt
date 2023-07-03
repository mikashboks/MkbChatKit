package com.stfalcon.chatkit.sample.features.main.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.stfalcon.chatkit.sample.R

/*
 * Created by troy379 on 11.04.17.
 */
class DemoCardFragment : Fragment(), View.OnClickListener {
    private var id = 0
    private var title: String? = null
    private var description: String? = null
    private var actionListener: OnActionListener? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            id = requireArguments().getInt(ARG_ID)
            title = requireArguments().getString(ARG_TITLE)
            description = requireArguments().getString(
                ARG_DESCRIPTION
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(
            R.layout.fragment_demo_card,
            container,
            false
        )
        val tvTitle = v.findViewById<TextView>(R.id.tvTitle)
        val tvDescription =
            v.findViewById<TextView>(R.id.tvDescription)
        val button = v.findViewById<Button>(R.id.button)
        tvTitle.text = title
        tvDescription.text = description
        button.setOnClickListener(this)
        return v
    }

    override fun onClick(view: View) {
        if (view.id == R.id.button) {
            onAction()
        }
    }

    fun onAction() {
        if (actionListener != null) {
            actionListener!!.onAction(id)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        actionListener = if (context is OnActionListener) {
            context
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement OnActionListener"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        actionListener = null
    }

    interface OnActionListener {
        fun onAction(id: Int)
    }

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_TITLE = "title"
        private const val ARG_DESCRIPTION = "description"
        fun newInstance(
            id: Int,
            title: String?,
            description: String?
        ): DemoCardFragment {
            val fragment = DemoCardFragment()
            val args = Bundle()
            args.putInt(ARG_ID, id)
            args.putString(ARG_TITLE, title)
            args.putString(ARG_DESCRIPTION, description)
            fragment.arguments = args
            return fragment
        }
    }
}