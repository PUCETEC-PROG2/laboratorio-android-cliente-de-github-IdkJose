package ec.edu.uisek.githubclient

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import ec.edu.uisek.githubclient.databinding.FragmentNewProjectBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewProjectFragment : Fragment() {

    private var _binding: FragmentNewProjectBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewProjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveNewProject.setOnClickListener {
            val projectName = binding.etNewProjectName.text.toString().trim()
            val projectDescription = binding.etNewProjectDetails.text.toString().trim()
            
            if (projectName.isNotBlank()) {
                createRepository(projectName, projectDescription)
            } else {
                binding.etNewProjectName.error = getString(R.string.hint_new_project_name)
            }
        }
    }

    private fun createRepository(name: String, description: String) {
        // Mostrar indicador de carga
        binding.btnSaveNewProject.isEnabled = false
        binding.btnSaveNewProject.text = "Creando..."

        val apiService = RetrofitClient.gitHubApiService
        val repoData = mutableMapOf(
            "name" to name,
            "description" to description,
            "private" to "false",
            "auto_init" to "true"
        )

        val call = apiService.createRepo(repoData)
        call.enqueue(object : Callback<Repo> {
            override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                // Restaurar botón
                binding.btnSaveNewProject.isEnabled = true
                binding.btnSaveNewProject.text = getString(R.string.btn_save)

                if (response.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Repositorio '${name}' creado exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // Notificar a MainActivity que recargue la lista
                    (activity as? MainActivity)?.refreshRepositories()
                    
                    // Cerrar el fragmento
                    parentFragmentManager.popBackStack()
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Error de autenticación"
                        403 -> "No tienes permisos para crear repositorios"
                        422 -> "El repositorio ya existe o los datos son inválidos"
                        else -> "Error al crear repositorio: ${response.code()}"
                    }
                    Log.e("NewProjectFragment", "Error: ${response.code()}")
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<Repo>, t: Throwable) {
                // Restaurar botón
                binding.btnSaveNewProject.isEnabled = true
                binding.btnSaveNewProject.text = getString(R.string.btn_save)
                
                Log.e("NewProjectFragment", "Error de conexión", t)
                Toast.makeText(
                    context,
                    "Error de conexión: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}