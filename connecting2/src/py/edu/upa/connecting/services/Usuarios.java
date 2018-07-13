package py.edu.upa.connecting.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import py.edu.upa.connecting.entidades.Usuario;

@Path("/usuarios")
@RequestScoped
public class Usuarios {

	/**
	 * Datasource para obtener conexion a base de datos
	 * @param member
	 * @return
	 */
	@Resource(lookup = "java:jboss/datasources/ConnectingDS")
	DataSource ds;


	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response crearUsuario(Usuario usuario) {

		Response.ResponseBuilder builder = null;
		Map<String, String> responseObj = new HashMap<String, String>();
		String pass;
		int id;

		try (Connection con = ds.getConnection();
				PreparedStatement ps = con.prepareStatement("insert into USUARIO (cod_usuario,nombre,telefono,email,password) "
						+ "values (?,?,?,?,?)")
				) {

			pass = usuario.getPassword();

			id=generarId();

			if (comprobarPassword(pass)) {
				ps.setInt(1, id);
				ps.setString(2, usuario.getNombre());
				ps.setString(3, usuario.getTelefono());
				ps.setString(4, usuario.getEmail());
				ps.setString(5, pass);

				ps.executeUpdate();

				responseObj.put("mensaje","Usuario con id: "+id+" cargado exitosamente");
				builder = Response.ok(responseObj);
			} else {
				responseObj.put("mensaje","La contrase�a debe tener al menos 8 caracteres");
				builder = Response.status(Response.Status.CONFLICT).entity(responseObj);
			}


		} catch (Exception e) {
			// Handle generic exceptions.
			
			if (e.getMessage().contains(" Ya existe la llave (email)")) {
				responseObj.put("mensaje", "El usuario el usuario con mail : " + usuario.getEmail());
				builder = Response.status(Response.Status.NOT_FOUND).entity(responseObj);
			}
				else {
					responseObj.put("error","Ocurrio el siguiente error: " +  e.getMessage());
					builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
				}
		}
		return builder.build();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response modificarUsuario(Usuario usuario) {

		Map<String, String> responseObj = new HashMap<String, String>();
		Response.ResponseBuilder builder = null;
		
		String pass;

		try (Connection con = ds.getConnection();
				PreparedStatement ps = con.prepareStatement("update USUARIO set nombre = ?,telefono = ?,email=?,password=? "
						+ "where cod_usuario = ?")
				) {
			
			pass = usuario.getPassword();
			
			if (comprobarPassword(pass)) {
				ps.setString(1, usuario.getNombre());
				ps.setString(2, usuario.getTelefono());
				ps.setString(3, usuario.getEmail());
				ps.setString(4, usuario.getPassword());
				ps.setInt(5, usuario.getCodUsuario());

				ps.executeUpdate();

				responseObj.put("mensaje","Usuario con id: "+usuario.getCodUsuario()+" modificado exitosamente");
				builder = Response.ok(responseObj);
			}	else {
				responseObj.put("mensaje","La contrase�a debe tener al menos 8 caracteres");
				builder = Response.status(Response.Status.CONFLICT).entity(responseObj);
			}


		} catch (Exception e) {
			// Handle generic exceptions.
			e.printStackTrace();
			responseObj.put("error","Ocurrio el siguiente error: " +  e.getMessage());
			builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
		}

		return builder.build();
	}

	@DELETE
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response eliminarUsuario(@PathParam("id") String codUsuario) {

		Response.ResponseBuilder builder = null;

		try (Connection con = ds.getConnection();
				PreparedStatement ps = con.prepareStatement("delete from USUARIO  "
						+ "where cod_usuario = ?")
				) {

			ps.setString(1, codUsuario);

			ps.executeUpdate();

			builder = Response.ok();

		} catch (Exception e) {
			// Handle generic exceptions.
			e.printStackTrace();
			Map<String, String> responseObj = new HashMap<String, String>();
			responseObj.put("error","Ocurrio el siguiente error: " +  e.getMessage());
			builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
		}

		return builder.build();
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Usuario obtenerUsuarios(@PathParam("id") String codUsuario) {

		System.out.println("Se va a buscar al usuario : [" + codUsuario + "]");

		try (Connection con = ds.getConnection();
				PreparedStatement ps = con.prepareStatement("select * from USUARIO  "
						+ "where cod_usuario = ? ")
				) {

			ps.setString(1, codUsuario);

			ResultSet rs = ps.executeQuery();
			ArrayList<Usuario> listaUsuario = cargarUsuarios(rs);

			if (listaUsuario.size() == 0)
				return null;
			else
				return listaUsuario.get(0);

		} catch (Exception e) {
			// Handle generic exceptions.
			e.printStackTrace();
			return null;
		}
	}


	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Usuario> obtenerUsuarios() {

		try (Connection con = ds.getConnection();
				PreparedStatement ps = con.prepareStatement("select * from USUARIO")
				) {

			ResultSet rs = ps.executeQuery();
			return cargarUsuarios(rs);

		} catch (Exception e) {
			// Handle generic exceptions.
			e.printStackTrace();
			return null;
		}
	}

	public static ArrayList<Usuario> cargarUsuarios(ResultSet rs) throws Exception {
		ArrayList<Usuario> listaUsuarios = new ArrayList<Usuario>();

		while(rs.next()) {
			Usuario usuarioActual = new Usuario();

			usuarioActual.setCodUsuario(rs.getInt("cod_usuario"));
			usuarioActual.setNombre(rs.getString("nombre"));
			usuarioActual.setTelefono(rs.getString("telefono"));
			usuarioActual.setEmail(rs.getString("email"));
			usuarioActual.setPassword("*******");  //Obs: No mostramos el password por seguridad y confidencialidad 

			listaUsuarios.add(usuarioActual);
		}

		return listaUsuarios;
	}

	public static boolean comprobarPassword(String password) {

		if(password.length()>=8) return true;
		else return false;
	}

	public int generarId() {
		int id;

		try ( Connection con = ds.getConnection();
				Statement st = con.createStatement();
				) {
			
			ResultSet rs = st.executeQuery("SELECT nextval('serial');");
			
			rs.next();
			
			id = rs.getInt(1);

		}catch (Exception e) {

			e.printStackTrace();
			return 0;
		}

		return id;
	}

}
