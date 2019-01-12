package com.kab.channel66

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.format.Time
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
class ServiceRegistrationFragment : Fragment() , AdapterView.OnItemSelectedListener{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private var name: EditText? =null
    private var email: EditText? =null
    private var number: EditText? =null
    private var group: Spinner? =null
    private var gender: RadioGroup? = null
    private var register:Button ?=null
    private var cancel:Button ?=null
    private var group_tv: TextView? = null



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
        val groups: Array<out String> = resources.getStringArray(R.array.groups_array)
        name = view.findViewById(R.id.input_name)
        email = view.findViewById(R.id.input_email)
        number = view.findViewById(R.id.input_tel)
        group = view.findViewById(R.id.input_group)
        group_tv = view.findViewById(R.id.input_group_text)
        gender = view.findViewById(R.id.input_gender)
        register = view.findViewById(R.id.btn_register)
        register?.setOnClickListener(clickListener)
        cancel?.setOnClickListener(cancelClickListener)




        ArrayAdapter.createFromResource(
                activity,
                R.array.groups_array,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            group?.adapter = adapter
        }



        return view;
    }


    val clickListener = View.OnClickListener {view ->

        var data:JSONObject  =  JSONObject()

        data.put("name",name?.text)
        data.put("email",email?.text)
        data.put("number",number?.text)
        data.put("group",group_tv?.text)
        data.put("gender", resources.getResourceEntryName(gender!!.checkedRadioButtonId))
        data.put("timezone", Time.getCurrentTimezone())
        for( key:String  in  data.keys())
        {
                if(data.getString(key).isEmpty())
                    showError()

        }
        if(!email.toString().isValidEmail())
            showEmailError()

        listener?.onFragmentInteraction(data)

        activity!!.supportFragmentManager.beginTransaction().remove(this).commit();
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        group_tv?.setText(parent.getItemAtPosition(pos).toString())
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }


    private fun showEmailError() {

        val builder = AlertDialog.Builder(this!!.context!!)

        // Set the alert dialog title
        builder.setTitle("Wrong input data")

        // Display a message on alert dialog
        builder.setMessage("Please fix email")

        // Set a positive button and its click listener on alert dialog



//        // Display a negative button on alert dialog
//        builder.setNegativeButton("No"){dialog,which ->
//            Toast.makeText(applicationContext,"You are not agree.",Toast.LENGTH_SHORT).show()
//        }
//
//
//        // Display a neutral button on alert dialog
//        builder.setNeutralButton("Cancel"){_,_ ->
//            Toast.makeText(applicationContext,"You cancelled the dialog.",Toast.LENGTH_SHORT).show()
//        }

        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()
    }

    private fun showError() {
        val builder = AlertDialog.Builder(this!!.context!!)

        // Set the alert dialog title
        builder.setTitle("Wrong input data")

        // Display a message on alert dialog
        builder.setMessage("Please make sure all fields are filled")

        // Set a positive button and its click listener on alert dialog



//        // Display a negative button on alert dialog
//        builder.setNegativeButton("No"){dialog,which ->
//            Toast.makeText(applicationContext,"You are not agree.",Toast.LENGTH_SHORT).show()
//        }
//
//
//        // Display a neutral button on alert dialog
//        builder.setNeutralButton("Cancel"){_,_ ->
//            Toast.makeText(applicationContext,"You cancelled the dialog.",Toast.LENGTH_SHORT).show()
//        }

        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val cancelClickListener = View.OnClickListener {view ->


        activity!!.supportFragmentManager.beginTransaction().remove(this).commit();
    }


    // TODO: Rename method, update argument and hook method into UI event


    fun String.isValidEmail(): Boolean
            = this.isNotEmpty() &&
            Patterns.EMAIL_ADDRESS.matcher(this).matches()


    private fun isEmpty(etText: EditText): Boolean {
        return if (etText.text.toString().trim { it <= ' ' }.length > 0) false else true

    }
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
