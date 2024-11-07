# SingUpSingIn

<h1>Repositorios de GitHub</h1>

Cliente 🡪 https://github.com/adriro20/SingUpSingInClient </br>
Servidor 🡪 https://github.com/adriro20/SingUpSingInServer </br>
Librería 🡪 https://github.com/adriro20/SingUpSingInLib </br>

<h1>Parámetros de los ficheros “.properties”</h1>

<h3>Ficheros del cliente:</h3>

<h5>infoClient.properties</h5>
<ul>
  <li>Puerto	→	Puerto de escucha del servidor.</li>
  <li>IP	→	IP del servidor.</li>
</ul>

<h3>Ficheros del servidor:</h3>

<h5>connections.properties</h5>
<ul>
  <li>TCON	→	Número máximo de conexiones simultáneas permitidas.</li>
  <li>URL	→	Url de conexión a la base de datos.</li>
  <li>USER	→	Usuario de la base de datos.</li>
  <li>PWD	→	Contraseña de la base de datos.</li>
</ul>

<h5>infoServer.properties</h5>
<ul>
  <li>PORT	→	Puerto de escucha del servidor.</li>
</ul>

<h1>Explicación de la función de los ficheros de test</h1>

<h3>SignInWindowControllerTest</h3>
Test general para la ventana de SignIn.

<h3>SignUpWindowControllerTest</h3>
Test general para la ventana de SignUp.

<h3>ServerClosedErrorTest</h3>
Test específico para comprobar la excepción “ServerClosedException”. Para que se ejecute de manera correcta hay que realizar una acción (SignUp/SignIn) sin el servidor arrancado.

<h3>InternalServerErrorTest</h3>
Test específico para comprobar la excepción “InternalServerErrorException”. Para que se ejecute de manera correcta hay que realizar una acción (SignUp/SignIn) sin el servidor de bases de datos arrancado o con una IP o puerto del servidor de base de datos incorrectos en el fichero “connections.properties”.

<h1>Preparación para la ejecución de las aplicaciones</h1>

<ol>
  <li>Es necesaria una base de datos de ODOO con los módulos “project” y “hr_timesheet” instalados.</li>
  <li>Lo primero que se debe de ejecutar para el correcto funcionamiento es el servidor.</li>
  <li>Una vez con el servidor en funcionamiento se podrá iniciar con normalidad el cliente.</li>
</ol>

<h3>Cosas a tener en cuenta</h3>
Al iniciar con una base de datos nueva no se podrá iniciar sesión sin registrar antes a algún usuario, esto aplica en los tests que hay que ejecutar iniciando de una base de datos desde cero el test de Sign Up antes que el test de Sign In.
