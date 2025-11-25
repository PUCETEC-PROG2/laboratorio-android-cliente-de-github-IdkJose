package ec.edu.uisek.githubclient

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.textfield.TextInputEditText
import ec.edu.uisek.githubclient.models.Repo

class EditRepoDialogFragment(
    private val repo: Repo,
    private val onSave: (name: String, description: String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_edit_repo, null)

        val editName = view.findViewById<TextInputEditText>(R.id.edit_repo_name)
        val editDescription = view.findViewById<TextInputEditText>(R.id.edit_repo_description)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        val btnSave = view.findViewById<Button>(R.id.btn_save)

        editName.setText(repo.name)
        editName.isEnabled = false
        editDescription.setText(repo.description ?: "")

        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .create()

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnSave.setOnClickListener {
            val newName = editName.text.toString().trim()
            val newDescription = editDescription.text.toString().trim()

            if (newName.isNotEmpty()) {
                onSave(newName, newDescription)
                dismiss()
            } else {
                editName.error = "El nombre no puede estar vacío"
            }
        }

        return dialog
    }
}
