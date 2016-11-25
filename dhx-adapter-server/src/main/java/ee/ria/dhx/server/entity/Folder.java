package ee.ria.dhx.server.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import javax.persistence.*;

import java.util.List;


/**
 * The persistent class for the kaust database table.
 * 
 */
@Entity
@Table(name = "kaust")
@Getter
@Setter
public class Folder  extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "kaust_id")
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Integer folderId;

  @Column(name = "kausta_number")
  private String folderNumber;

  @Column(name = "nimi")
  private String name;


  // bi-directional many-to-one association to Kaust
  @ManyToOne
  @JoinColumn(name = "ylemkaust_id")
  private Folder parentFolder;

  // bi-directional many-to-one association to Kaust
  @OneToMany(mappedBy = "parentFolder")
  private List<Folder> childFolders;

  public Folder() {}



  public Folder addChildFolder(Folder childFolder) {
    getChildFolders().add(childFolder);
    childFolder.setParentFolder(this);

    return childFolder;
  }

  public Folder removeChildFolder(Folder childFolder) {
    getChildFolders().remove(childFolder);
    childFolder.setParentFolder(null);

    return childFolder;
  }


}
