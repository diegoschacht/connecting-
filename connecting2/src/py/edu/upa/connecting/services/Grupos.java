/**
 * prueba 2
 */
package py.edu.upa.connecting.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.sql.Date;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.security.auth.callback.DatabaseCallbackHandler;

import py.edu.upa.connecting.entidades.Grupo;
import py.edu.upa.connecting.entidades.Usuario;

@Path("/grupos")
@RequestScoped
public class Grupos {

	/**
	 * Datasource para obtener conexion a base de datos
	 * 
	 * @param member
	 * @return
	 */
	@Resource(lookup = "java:jboss/datasources/ConnectingDS")
	DataSource ds;

	@DELETE
	@Path("/{id_grupo}/integrantes/{id_usuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response abandonarGrupo(@PathParam("id_usuario") String codUsuario, @PathParam("id_grupo") int codGrupo) {

		Map<String, String> responseObj = new HashMap<String, String>();
		Response.ResponseBuilder builder = null;

		try (Connection con = ds.getConnection();
				PreparedStatement ps = con.prepareStatement(
						"delete from integrantes_grupo " + "where cod_usuario = ? " + "and cod_grupo = ?;")) {

			ps.setString(1, codUsuario);
			ps.setInt(2, codGrupo);

			if (ps.executeUpdate() != 0) {
				responseObj.put("mensaje", "El usuario " + codUsuario + " abandono el grupo " + codGrupo);
				builder = Response.ok(responseObj);
			} else {
				responseObj.put("mensaje", "No se encontró el grupo " + codGrupo + " o usuario " + codUsuario);
				builder = Response.status(Response.Status.NOT_FOUND).entity(responseObj);
			}

		} catch (Exception e) {
			e.printStackTrace();

			responseObj.put("error", "Ocurrio el siguiente error: " + e.getMessage());
			builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
		}

		return builder.build();

	}

	@DELETE

	@Path("/{id_grupo}")

	@Produces(MediaType.APPLICATION_JSON)
	public Response eliminarGrupo(@PathParam("id_grupo") int codGrupo) {

		Map<String, String> responseObj = new HashMap<String, String>();
		Response.ResponseBuilder builder = null;

		try (Connection con = ds.getConnection();
				PreparedStatement ps = con
						.prepareStatement("delete from integrantes_grupo " + "where cod_grupo = ?")) {

			ps.setInt(1, codGrupo);

			PreparedStatement pt = con
					.prepareStatement("delete from grupo " + "where cod_grupo = ?");

			pt.setInt(1, codGrupo);

			int cant_int = cantidadFilasTabla("integrantes_grupo", codGrupo, con);
			if (cant_int < 1) {

				if(pt.executeUpdate() !=0) {
					responseObj.put("mensaje", "Grupo " + codGrupo + " eliminado correctamente");
					builder = Response.ok(responseObj);
					}
				else {
					responseObj.put("mensaje", "No se encontró grupo con el código " + codGrupo + "");
					builder = Response.status(Response.Status.NOT_FOUND).entity(responseObj);
				}

			}else {
				responseObj.put("mensaje", "“No se puede eliminar el grupo " + codGrupo + " este ya tiene " + 
						"integrantes");
				builder = Response.status(Response.Status.CONFLICT).entity(responseObj);
			}

		} catch (Exception e) {
			e.printStackTrace();

			responseObj.put("error", "Ocurrio el siguiente error: " + e.getMessage());
			builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
		}

		return builder.build();

	}

	@POST
	@Path("/{id_grupo}/integrantes/{id_usuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response unirseGrupo(@PathParam("id_usuario") int codUsuario, @PathParam("id_grupo") int codGrupo) {

		Map<String, String> responseObj = new HashMap<String, String>();

		Response.ResponseBuilder builder = null;

		try (Connection con = ds.getConnection();
				PreparedStatement ps = con
						.prepareStatement("insert into integrantes_grupo (cod_usuario, cod_grupo) values (?,?);")) {

			ps.setInt(1, codUsuario);
			ps.setInt(2, codGrupo);

			ps.executeUpdate();

			responseObj.put("mensaje", "El usuario " + codUsuario + " se unio al grupo " + codGrupo + " exitosamente");
			builder = Response.ok(responseObj);

		} catch (Exception e) {
			// Handle generic exceptions.

			e.printStackTrace();

			if (e.getMessage().contains("no está presente en la tabla «grupo».")) {
				responseObj.put("mensaje", "No se encontro el grupo con el codigo: " + codGrupo);
				builder = Response.status(Response.Status.NOT_FOUND).entity(responseObj);
			} else if (e.getMessage().contains("no está presente en la tabla «usuario».")) {
				responseObj.put("mensaje", "No se encontro el usuario con el codigo: " + codUsuario);
				builder = Response.status(Response.Status.NOT_FOUND).entity(responseObj);
			} else if (e.getMessage().contains("Ya existe la llave (cod_usuario, cod_grupo)")) {
				responseObj.put("mensaje", "El usuario " + codUsuario + " ya pertenece al grupo " + codGrupo);
				builder = Response.status(Response.Status.NOT_FOUND).entity(responseObj);
			} else {
				responseObj.put("error", "Ocurrio el siguiente error: " + e.getMessage());
				builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
			}

		}

		return builder.build();
	}

	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response modificarGrupo(Grupo grupo, @PathParam("id") int codGrupo) {

		Map<String, String> responseObj = new HashMap<String, String>();

		Response.ResponseBuilder builder = null;

		try (Connection con = ds.getConnection();
				PreparedStatement ps = con.prepareStatement(
						"update GRUPO set nombre = ?, objetivo = ?,longitud=?, latitud=?" + "where cod_grupo = ?")) {

			ps.setString(1, grupo.getNombre());
			ps.setString(2, grupo.getObjetivo());
			ps.setInt(3, grupo.getLongitud());
			ps.setInt(4, grupo.getLatitud());
			ps.setLong(5, codGrupo);

			if (ps.executeUpdate() != 0) {
				responseObj.put("mensaje", "Grupo modificado exitosamente");
				builder = Response.ok(responseObj);
			} else {
				responseObj.put("mensaje", "No se encontró grupo con el código: " + codGrupo);
				builder = Response.status(Response.Status.NOT_FOUND).entity(responseObj);
			}

		} catch (Exception e) {
			// Handle generic exceptions.
			e.printStackTrace();
			responseObj.put("error", "Ocurrio el siguiente error: " + e.getMessage());
			builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
		}

		return builder.build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Grupo> obtenerUsuarios(@QueryParam("latitud") Integer latitud, @QueryParam("longitud") Integer longitud,
			@QueryParam("cod_usuario") String codUsuario) {

		String sql = "select * from GRUPO where 1 = 1 ";

		if (latitud != null)
			sql += "and latitud = " + latitud;

		if (longitud != null)
			sql += "and longitud = " + longitud;

		if (codUsuario != null)
			sql += "and codUsuario = " + codUsuario;

		try (Connection con = ds.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ResultSet rs = ps.executeQuery();
			return cargarGrupos(rs);

		} catch (Exception e) {
			// Handle generic exceptions.
			e.printStackTrace();
			return null;
		}
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response obtenerGrupo(@PathParam("id") String codGrupo) {

		System.out.println("Se va a buscar al grupo  : [" + codGrupo + "]");

		try (Connection con = ds.getConnection();
				PreparedStatement ps = con.prepareStatement("select * from GRUPO  " + "where cod_grupo = ?")) {

			ps.setLong(1, Long.parseLong(codGrupo));

			ResultSet rs = ps.executeQuery();
			ArrayList<Grupo> listaGrupo = cargarGrupos(rs);

			if (listaGrupo.size() == 0)
				return Response.status(Response.Status.NOT_FOUND).build();
			else
				return Response.ok(listaGrupo.get(0)).build();

		} catch (Exception e) {
			// Handle generic exceptions.
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	@GET
	@Path("{id_grupo}/integrantes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response obtenerIntegrantesGrupo(@PathParam("id_grupo") Integer codGrupo) {

		try (Connection con = ds.getConnection();
				PreparedStatement ps = con.prepareStatement(
						"select * from usuario inner join integrantes_grupo on integrantes_grupo.cod_usuario = usuario.cod_usuario \r\n"
								+ "where integrantes_grupo.cod_grupo = ?")) {

			ps.setLong(1, codGrupo);
			ResultSet rs = ps.executeQuery();
			ArrayList<Usuario> listaGrupo = Usuarios.cargarUsuarios(rs);

			if (listaGrupo.size() == 0) {
				System.out.println("Grupo Vacio");
				return Response.status(Response.Status.NOT_FOUND).build();
			} else
				return Response.ok(listaGrupo).build();
		} catch (Exception e) {
			// Handle generic exceptions.
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response crearGrupo(Grupo grupo) {

		Response.ResponseBuilder builder = null;

		try (Connection con = ds.getConnection();
				PreparedStatement ps = con.prepareStatement(
						"insert into GRUPO (cod_grupo,nombre,objetivo,longitud,latitud,cod_usuario_creacion,fecha_creacion) "
								+ "values (?,?,?,?,?,?,?)")) {

			ps.setLong(1, grupo.getCodGrupo());
			ps.setString(2, grupo.getNombre());
			ps.setString(3, grupo.getObjetivo());
			ps.setInt(4, grupo.getLongitud());
			ps.setInt(5, grupo.getLatitud());
			ps.setInt(6, grupo.getCodUsuarioCreacion());
			ps.setDate(7, new Date(System.currentTimeMillis()));

			ps.executeUpdate();

			builder = Response.ok();

		} catch (Exception e) {
			// Handle generic exceptions.
			e.printStackTrace();
			Map<String, String> responseObj = new HashMap<String, String>();
			responseObj.put("error", "Ocurrio el siguiente error: " + e.getMessage());
			builder = Response.status(Response.Status.BAD_REQUEST).entity(responseObj);
		}

		return builder.build();
	}

	private ArrayList<Grupo> cargarGrupos(ResultSet rs) throws Exception {
		ArrayList<Grupo> listaGrupos = new ArrayList<Grupo>();

		while (rs.next()) {
			Grupo grupoActual = new Grupo();

			grupoActual.setCodGrupo(rs.getLong("cod_grupo"));
			grupoActual.setNombre(rs.getString("nombre"));
			grupoActual.setObjetivo(rs.getString("objetivo"));
			grupoActual.setLatitud(rs.getInt("latitud"));
			grupoActual.setLongitud(rs.getInt("longitud"));
			grupoActual.setCodUsuarioCreacion(rs.getInt("cod_usuario_creacion"));
			grupoActual.setFechaUsuarioCreacion(rs.getDate("fecha_creacion"));

			listaGrupos.add(grupoActual);
		}

		return listaGrupos;
	}

	public static int cantidadFilasTabla(String nombre_tabla, int cod_grupo, Connection con) throws Exception {

		Statement stmt = con.createStatement();
		int resultado = 0;

		ResultSet rs = stmt
				.executeQuery("select count(1)as cant from " + nombre_tabla + " where cod_grupo =" + cod_grupo );

		while (rs.next()) {
			resultado = rs.getInt("cant");
		}
		stmt.close();
		return resultado;
	}

}
