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
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class Create_Account : AppCompatActivity() {
    lateinit var editEmail: EditText;
    lateinit var editPassword: EditText;
    lateinit var editConfirmPassword: EditText;

    val Req_Code:Int=123;
    lateinit var mGoogleSignInClient: GoogleSignInClient;
    private lateinit var firebaseAuth: FirebaseAuth;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        supportActionBar?.hide();

        /* Iniciação da autenticação com o firebase */
        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance();

        editEmail = findViewById<EditText>(R.id.editEmail)
        editPassword = findViewById<EditText>(R.id.editPassword)
        editConfirmPassword = findViewById<EditText>(R.id.editConfirmPassword)

        val objetoLoginPadrao = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();

        /* Passa o contexto atual do aplicativo e o objeto "GoogleSignInOptions" */
        mGoogleSignInClient = GoogleSignIn.getClient(this, objetoLoginPadrao);

        findViewById<View>(R.id.btnEntrar).setOnClickListener{
            val activity = Intent(this, Login_Screen::class.java);
            startActivity(activity);
        }

        /*  Toast = mostra mensagens curtas ao usuário, com feedback visual
            Vai buscar a mensagem no string.xml
         */
        findViewById<View>(R.id.btnRegistrarGoogle).setOnClickListener {
            Toast.makeText(this, R.string.registro_com_google, Toast.LENGTH_SHORT).show()
            entrarComGoogle();
        }

        findViewById<View>(R.id.btnRegistrar).setOnClickListener {
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
            Toast.makeText(this, "Logado com sucesso.", Toast.LENGTH_SHORT).show();
            if(account != null){
                UpdateUser(account)
            }
        }catch (e: ApiException){
            println(e)
            Toast.makeText(this, "Falha ao logar", Toast.LENGTH_SHORT).show();
        }
    }

    private fun UpdateUser(account: GoogleSignInAccount){
        /* Verificar a credencial com o google */
        val credential = GoogleAuthProvider.getCredential(account.idToken, null);
        /* Autenticar o usuario com o firebase authentication */
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
            editEmail.text.toString().trim().isNotEmpty() &&
            editConfirmPassword.text.toString().trim().isNotEmpty()

    private fun SenhasIguais(): Boolean{
        return (editPassword.text.toString().trim() == editConfirmPassword.text.toString().trim());
    }

    private fun TamanhoDaSenhaValida(): Boolean{
        return (editPassword.text.toString().trim().length >= 6);
    }

    private fun EnviarEmailVerificacao(){
        val firebaseUser: FirebaseUser? = firebaseAuth.currentUser

        firebaseUser?.let {
            it.sendEmailVerification().addOnCompleteListener { task ->
                if(task.isSuccessful){
                    Toast.makeText(this, "E-mail enviado com sucesso!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    private fun Entrar(){
        if(CampoVazio()){
            if(SenhasIguais()){
                if(TamanhoDaSenhaValida()){
                    val userEmail = editEmail.text.toString().trim()
                    val userPassword = editPassword.text.toString().trim()

                    firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener{task ->
                        if(task.isSuccessful){
                            EnviarEmailVerificacao()
                            Toast.makeText(this, "Usuário criado com sucesso. Verifique sua caixa de e-mail", Toast.LENGTH_SHORT).show();
                            finish()
                        }else{
                            val exception = task.exception;
                            if(exception is FirebaseAuthException && exception.errorCode == "ERROR_EMAIL_ALREADY_IN_USE"){
                                Toast.makeText(this, "Email já cadastrado.", Toast.LENGTH_SHORT).show();
                            }else if(exception is FirebaseAuthException && exception.errorCode == "ERROR_WEAK_PASSWORD"){
                                Toast.makeText(this, "Senha fraca.", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(this, "Erro ao criar usuário.", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                }else{
                    Toast.makeText(this, "As senhas são iguais mas de tamanho inadequado.", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
        }
    }

}