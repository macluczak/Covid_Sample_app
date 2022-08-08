package eu.inkbook.sample.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import eu.inkbook.sample.data.DataEntity
import eu.inkbook.sample.databinding.EntityHolderBinding

// ListView has been replaced by the currently recommended way to display views, RecyclerView

class EntityAdapter(val items: ArrayList<DataEntity>): RecyclerView.Adapter<EntityAdapter.EntityHolder>() {
    inner class EntityHolder (val binding: EntityHolderBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntityHolder  {
        return EntityHolder(EntityHolderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: EntityHolder, position: Int) {
        val item = items[position]

        holder.binding.apply {
            country.text = item.countriesAndTerritories
            culmulative.text = String.format("%.3f", item.Cumulative_number)
            date.text = "${item.day}/${item.month}/${item.year}"
            continent.text = "${item.continentExp},"
            cases.text = item.cases.toString()
            death.text = item.deaths.toString()
            geoId.text = item.geoId


        }

    }
    override fun getItemCount(): Int {
        return items.size
    }

}
