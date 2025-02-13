package com.example.bookhive.filters

import android.widget.Filter
import com.example.bookhive.Adapter.AdapterPdfAdmin
import com.example.bookhive.Models.ModelPdf

class FilterPdfAdmin : Filter {
    //arraylist in which we want to search
    private var filterList: ArrayList<ModelPdf>

    //adapter in which filter need to be implemented
    private var adapterPdfAdmin: AdapterPdfAdmin

    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint: CharSequence? = constraint
        val results = FilterResults()

        //value should not be null and not empty
        if (constraint != null && constraint.isNotEmpty()) {
            //searched value is nor null not empty
            //change to lower case , or longer case to avoid case sensitivity
            constraint = constraint.toString().lowercase()
            val filteredModels: ArrayList<ModelPdf> = ArrayList()
            for (i in filterList.indices) {
                //validate
                if (filterList[i].title.lowercase().contains(constraint)) {
                    //add to filtered list
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        //apply filter changes
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelPdf>

        //notify changes
        adapterPdfAdmin.notifyDataSetChanged()
    }


}