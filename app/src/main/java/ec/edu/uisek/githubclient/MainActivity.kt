package ec.edu.uisek.githubclient

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ec.edu.uisek.githubclient.databinding.ActivityMainBinding
import ec.edu.uisek.githubclient.models.Repo
import ec.edu.uisek.githubclient.services.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var reposAdapter: ReposAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFab()

        // Solo cargar repositorios si no hay un estado guardado (para evitar recargar al rotar)
        if (savedInstanceState == null) {
            fetchRepositories()
        }

        // Escuchar cambios en la pila de fragments para mostrar/ocultar el FAB
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount > 0) {
                // CAMBIO 1: fabAddProject -> fabNewTask
                binding.fabNewTask.hide()
            } else {
                // CAMBIO 1: fabAddProject -> fabNewTask
                binding.fabNewTask.show()
            }
        }
    }

    private fun setupRecyclerView() {
        reposAdapter = ReposAdapter(
            onEditClick = { repo -> showEditDialog(repo) },
            onDeleteClick = { repo -> deleteRepository(repo) }
        )
        binding.repoRecyclerView.apply {
            adapter = reposAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupFab() {
        // CAMBIO 1: fabAddProject -> fabNewTask
        binding.fabNewTask.setOnClickListener {
            // CAMBIO 2: NewProjectFragment (ya estaba correcto en tu código)
            val fragment = NewProjectFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun fetchRepositories() {
        val apiService = RetrofitClient.gitHubApiService
        val call = apiService.getRepos()

        call.enqueue(object : Callback<List<Repo>> {
            override fun onResponse(call: Call<List<Repo>>, response: Response<List<Repo>>) {
                if (response.isSuccessful) {
                    val repos = response.body()
                    if (repos != null && repos.isNotEmpty()) {
                        reposAdapter.updateRepositories(repos)
                    } else {
                        showMessage("No se encontraron repositorios")
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Error de autenticación. Revisa tu token."
                        403 -> "Prohibido"
                        404 -> "No encontrado"
                        else -> "Error: ${response.code()}"
                    }
                    Log.e("MainActivity", errorMsg)
                    showMessage(errorMsg)
                }
            }

            override fun onFailure(call: Call<List<Repo>>, t: Throwable) {
                //no hay conexion a red
                Log.e("MainActivity", "Error de conexión", t)
                showMessage("Error de conexión: ${t.message}")
            }
        })
    }

    fun refreshRepositories() {
        fetchRepositories()
    }

    private fun showMessage(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    private fun showEditDialog(repo: Repo) {
        val dialog = EditRepoDialogFragment(repo) { newName, newDescription ->
            updateRepository(repo, newName, newDescription)
        }
        dialog.show(supportFragmentManager, "EditRepoDialog")
    }

    private fun updateRepository(repo: Repo, newName: String, newDescription: String) {
        val apiService = RetrofitClient.gitHubApiService
        val updateData = mutableMapOf<String, String>()
        
        if (newName != repo.name) {
            updateData["name"] = newName
        }
        if (newDescription != (repo.description ?: "")) {
            updateData["description"] = newDescription
        }

        if (updateData.isEmpty()) {
            showMessage("No hay cambios para guardar")
            return
        }

        val call = apiService.updateRepo(repo.owner.login, repo.name, updateData)
        call.enqueue(object : Callback<Repo> {
            override fun onResponse(call: Call<Repo>, response: Response<Repo>) {
                if (response.isSuccessful) {
                    showMessage("Repositorio actualizado exitosamente")
                    fetchRepositories()
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Error de autenticación"
                        403 -> "No tienes permisos para editar este repositorio"
                        404 -> "Repositorio no encontrado"
                        422 -> "Datos inválidos. El nombre puede estar en uso"
                        else -> "Error al actualizar: ${response.code()}"
                    }
                    Log.e("MainActivity", "Error al actualizar: ${response.code()}")
                    showMessage(errorMsg)
                }
            }

            override fun onFailure(call: Call<Repo>, t: Throwable) {
                Log.e("MainActivity", "Error de conexión al actualizar", t)
                showMessage("Error de conexión: ${t.message}")
            }
        })
    }

    private fun deleteRepository(repo: Repo) {
        val apiService = RetrofitClient.gitHubApiService
        val call = apiService.deleteRepo(repo.owner.login, repo.name)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful || response.code() == 204) {
                    showMessage("Repositorio '${repo.name}' eliminado exitosamente")
                    fetchRepositories()
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "Error de autenticación"
                        403 -> "No tienes permisos para eliminar este repositorio"
                        404 -> "Repositorio no encontrado"
                        else -> "Error al eliminar: ${response.code()}"
                    }
                    Log.e("MainActivity", "Error al eliminar: ${response.code()}")
                    showMessage(errorMsg)
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("MainActivity", "Error de conexión al eliminar", t)
                showMessage("Error de conexión: ${t.message}")
            }
        })
    }
}