package com.example.finalprojectclassroom

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseBooleanArray
import android.view.View
import android.widget.ArrayAdapter
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences;
    private var gson = Gson();
    lateinit var mGoogleSignClient: GoogleSignInClient;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide();

        /*
            Declaração para armanezar e recuperar dados de um arquivo XML privado na memoria do dispositivo.
            E o nome lista_de_tarefas será o nome utilizado para armazenar.
            Context.MODE_PRIVATE = Significa que só pode ser acessado pelo aplicativo que o criou
        */
        sharedPreferences = getSharedPreferences("lista_de_tarefas", Context.MODE_PRIVATE);

        /*
           Criação de uma lista de visualização
           findViewById = Encontra elemento especifico pelo ID, no caso "ListaDeVisualizacao"
           <android.widget.ListView> = Referencia angular que informa ao compilador qual o tipo esperado do elemento
        */
        val listViewTasks = findViewById<android.widget.ListView>(R.id.ListaDeVisualizacao);

        /* Onde será informada a tarefa */
        val createTask = findViewById<android.widget.EditText>(R.id.editTextTarefa);

        /* Recebe uma arrayJson da função */
        val itemList = getDadosArray();

        /*
            - Criação de um adaptador que preenche os dados obtidos da função, e após, exibe cada item da lista,
            com o argumento "android.R.layout.simple_list_item_multiple_choice"
         */
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, itemList);

        /*  Contem as informações de configuração da API do google */
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();

        /* Inicia o fluxo de autenticação do google */
        mGoogleSignClient = GoogleSignIn.getClient(this, gso);

        /*  Autenticação pelo Firebase */
        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance();

        /*  Conecta um conjunto de dados a uma view  */
        listViewTasks.adapter = adapter;

        /* Apenas faz a atualização do conjunto de dados da View */
        adapter.notifyDataSetChanged();

        /* Adiciona a lista a view */
        findViewById<View>(R.id.btnAdicionar).setOnClickListener {
            itemList.add(createTask.text.toString());
            listViewTasks.adapter = adapter;
            adapter.notifyDataSetChanged()

            saveDadosArray(itemList)
            createTask.text.clear()
        }

        /* Remove a lista da view */
        findViewById<View>(R.id.btnDeletar).setOnClickListener {
            val position: SparseBooleanArray = listViewTasks.checkedItemPositions;
            val count = listViewTasks.count;
            var item = count - 1;
            while(item >= 0){
                if(position.get(item)){
                    adapter.remove(itemList.get(item))
                }
                item--;
            }

            saveDadosArray(itemList)
            position.clear();
            adapter.notifyDataSetChanged()
        }

        /* Limpa todas as tarefas */
        findViewById<View>(R.id.btnLimpar).setOnClickListener{
            itemList.clear()
            saveDadosArray(itemList)
            adapter.notifyDataSetChanged()
        }

        /* faz o logout */
        findViewById<View>(R.id.logout).setOnClickListener{
            firebaseAuth.signOut();
            mGoogleSignClient.signOut();

            val activity = Intent(this, Login_Screen::class.java);
            startActivity(activity);
        }
    }

    /*
        - Recebe uma lista de strings do objeto SharedPreferences armazenado no android,
        que vem da chave "lista" e armazena na variavel arrayJson
        - Depois verifica se o retorno da variavel é vazio/null, e se for, retorna uma arraylist vazio,
        senão, usa a biblioteca Gson para converter o JSON em arrayJson em uma lista de strings;
     */
    private fun getDadosArray(): ArrayList<String> {
        val arrayJson = sharedPreferences.getString("lista", null);
        return if (arrayJson.isNullOrEmpty()) {
            arrayListOf();
        } else {
            gson.fromJson(arrayJson, object : TypeToken<ArrayList<String>>() {}.type)
        }
    }

    private fun saveDadosArray(array: ArrayList<String>){
        val arrayJson = gson.toJson(array);
        val editor = sharedPreferences.edit();
        editor.putString("lista", arrayJson);
        editor.apply();
    }
}