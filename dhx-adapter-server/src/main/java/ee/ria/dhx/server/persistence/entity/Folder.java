package ee.ria.dhx.server.persistence.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The persistent class for the kaust database table.
 * 
 */
@Entity
@Table(name = "kaust")
public class Folder extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "kaust_id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer folderId;

  @Column(name = "kausta_number", unique = true)
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

  /**
   * Adds childFolder to array.
   * 
   * @param childFolder folder to add
   * @return the folder
   */
  public Folder addChildFolder(Folder childFolder) {
    getChildFolders().add(childFolder);
    childFolder.setParentFolder(this);

    return childFolder;
  }

  /**
   * Removes childFolder from array.
   * 
   * @param childFolder folder to remove
   * @return the folder
   */
  public Folder removeChildFolder(Folder childFolder) {
    getChildFolders().remove(childFolder);
    childFolder.setParentFolder(null);

    return childFolder;
  }

  /**
   * Returns the folderId.
   *
   * @return the folderId
   */
  public Integer getFolderId() {
    return folderId;
  }

  /**
   * Sets the folderId.
   *
   * @param folderId the folderId to set
   */
  public void setFolderId(Integer folderId) {
    this.folderId = folderId;
  }

  /**
   * Returns the folderNumber.
   *
   * @return the folderNumber
   */
  public String getFolderNumber() {
    return folderNumber;
  }

  /**
   * Sets the folderNumber.
   *
   * @param folderNumber the folderNumber to set
   */
  public void setFolderNumber(String folderNumber) {
    this.folderNumber = folderNumber;
  }

  /**
   * Returns the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the parentFolder.
   *
   * @return the parentFolder
   */
  public Folder getParentFolder() {
    return parentFolder;
  }

  /**
   * Sets the parentFolder.
   *
   * @param parentFolder the parentFolder to set
   */
  public void setParentFolder(Folder parentFolder) {
    this.parentFolder = parentFolder;
  }

  /**
   * Returns the childFolders.
   *
   * @return the childFolders
   */
  public List<Folder> getChildFolders() {
    return childFolders;
  }

  /**
   * Sets the childFolders.
   *
   * @param childFolders the childFolders to set
   */
  public void setChildFolders(List<Folder> childFolders) {
    this.childFolders = childFolders;
  }

}
