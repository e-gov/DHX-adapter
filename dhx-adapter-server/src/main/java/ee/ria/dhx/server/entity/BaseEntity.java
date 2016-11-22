package ee.ria.dhx.server.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import javax.persistence.MappedSuperclass;

@Getter
@Setter
@MappedSuperclass
public class BaseEntity {
  
  Date dateCreated;
  Date dateModified;
  

}
