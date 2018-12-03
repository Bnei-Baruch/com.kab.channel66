package com.kab.channel66

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.format.Time
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import org.json.JSONObject


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ServiceRegistrationFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ServiceRegistrationFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ServiceRegistrationFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var name: EditText? =null
    private var email: EditText? =null
    private var number: EditText? =null
    private var group: EditText? =null
    private var gender: RadioGroup? = null
    private var register:Button ?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        var view: View = inflater.inflate(R.layout.fragment_registration, container, false)

        name = view.findViewById(R.id.input_name)
        email = view.findViewById(R.id.input_email)
        number = view.findViewById(R.id.input_tel)
        group = view.findViewById(R.id.input_group)
        gender = view.findViewById(R.id.input_gender)
        register = view.findViewById(R.id.btn_register)
        register?.setOnClickListener(clickListener)


        return view;
    }


    val clickListener = View.OnClickListener {view ->

        var data:JSONObject  =  JSONObject()

        data.put("name",name?.text)
        data.put("email",email?.text)
        data.put("number",number?.text)
        data.put("group",group?.text)
        data.put("gender", resources.getResourceEntryName(gender!!.checkedRadioButtonId))
        data.put("timezone", Time.getCurrentTimezone())
        listener?.onFragmentInteraction(data)

        activity!!.supportFragmentManager.beginTransaction().remove(this).commit();
    }


    // TODO: Rename method, update argument and hook method into UI event


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(data: JSONObject)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BlankFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ServiceRegistrationFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
