package uk.gov.hmcts.dm.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Blob;
import java.util.Date;
import javax.persistence.*;

/**
 * Created by pawel on 08/06/2017.
 */
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DocumentContent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Getter
    @Setter
    private Long id;

    @JsonIgnore
    @Getter
    @Setter
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private Blob data;

    @OneToOne
    @Getter
    @Setter
    private DocumentContentVersion documentContentVersion;

    @Getter
    @Setter
    @CreatedBy
    private String createdBy;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;



    public DocumentContent(Blob blob) {
        this.data = blob;
    }

    public DocumentContent(DocumentContentVersion documentContentVersion, Blob data) {
        this(data);
        this.documentContentVersion = documentContentVersion;
    }


    public Date getCreatedOn() {
        return (createdOn == null) ? null : new Date(createdOn.getTime());
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = (createdOn == null) ? null : new Date(createdOn.getTime());
    }

}
