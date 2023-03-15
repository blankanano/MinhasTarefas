package com.example.finalprojectclassroom

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class Login_Screen : AppCompatActivity() {
    lateinit var editEmail: EditText;
    lateinit var editPassword: EditText;

    val Req_Code:Int=123;
    lateinit var mGoogleSignInClient: GoogleSignInClient;
    private lateinit var firebaseAuth: FirebaseAuth;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)
        supportActionBar?.hide();

        editEmail = findViewById<EditText>(R.id.editEmail)
        editPassword = findViewById<EditText>(R.id.editPassword)

        val objetoLoginPadrao = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();

        /* Passa o contexto atual do aplicativo e o objeto "GoogleSignInOptions" */
        mGoogleSignInClient = GoogleSignIn.getClient(this, objetoLoginPadrao);

        firebaseAuth = FirebaseAuth.getInstance();

        findViewById<View>(R.id.btnRegistrarGoogle).setOnClickListener{
            val activity = Intent(this, Login_Screen::class.java);
            startActivity(activity);
        }

        /*  Toast = mostra mensagens curtas ao usuário, com feedback visual
            Vai buscar a mensagem no string.xml
         */
        findViewById<View>(R.id.btnEntrarGoogle).setOnClickListener {
            Toast.makeText(this, R.string.login_com_google, Toast.LENGTH_SHORT).show()
            entrarComGoogle();
        }

        findViewById<View>(R.id.btnEntrar).setOnClickListener {
            Entrar()
        }
    }

    private fun entrarComGoogle(){
        val signIntent: Intent = mGoogleSignInClient.signInIntent;
        startActivityForResult(signIntent, Req_Code);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == Req_Code){
            val result = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleResult(result);
        }
    }

    /*  Verificar tentativa de login
        Task = Representa a tarefa concluida da tentativa
        getResult = verifica se a tarefa foi concluida, e o resultado é do tipo GoogleSignInAccount
    */
    private fun handleResult(completedTask: Task<GoogleSignInAccount>){
        try{
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java);
            Toast.makeText(this, "Logado com sucesso", Toast.LENGTH_SHORT).show();
            if(account != null){
                UpdateUser(account)
            }
        }catch (e: ApiException){
            println(e)
            Toast.makeText(this, "Falha ao logar", Toast.LENGTH_SHORT).show();
        }
    }

    private fun UpdateUser(account: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(account.idToken, null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {task ->
            if(task.isSuccessful){
                val intent = Intent(this, MainActivity::class.java);
                startActivity(intent);
                finish()
            }
        }
    }

    /* Validação para campo vazio */
    private fun CampoVazio(): Boolean = editPassword.text.toString().trim().isNotEmpty() &&
            editEmail.text.toString().trim().isNotEmpty();

    private fun Entrar(){
        if(CampoVazio()){
            val userEmail = editEmail.text.toString().trim()
            val userPassword = editPassword.text.toString().trim()

            firebaseAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val firebaseUser: FirebaseUser? = firebaseAuth.currentUser
                    if(firebaseUser != null && firebaseUser.isEmailVerified()){
                        startActivity(Intent(this, MainActivity::class.java))
                        Toast.makeText(this, "Logado com sucesso", Toast.LENGTH_SHORT).show()
                        finish()
                    }else if(firebaseUser != null && !firebaseUser.isEmailVerified()){
                        firebaseAuth.signOut()
                        Toast.makeText(this, "Verifique seu email", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "Falha ao logar", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(this, "Falha ao logar", Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
        }
    }

}