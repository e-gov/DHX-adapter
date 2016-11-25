package ee.ria.dhx.server.entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the server database table.
 * 
 */
@Entity
@NamedQuery(name="Server.findAll", query="SELECT s FROM Server s")
public class Server implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="server_id")
	private Integer serverId;

	private String aadress;

	@Column(name="andmekogu_nimi")
	private String andmekoguNimi;

	public Server() {
	}

	public Integer getServerId() {
		return this.serverId;
	}

	public void setServerId(Integer serverId) {
		this.serverId = serverId;
	}

	public String getAadress() {
		return this.aadress;
	}

	public void setAadress(String aadress) {
		this.aadress = aadress;
	}

	public String getAndmekoguNimi() {
		return this.andmekoguNimi;
	}

	public void setAndmekoguNimi(String andmekoguNimi) {
		this.andmekoguNimi = andmekoguNimi;
	}

}