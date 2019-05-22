package ingokuba.wolpertinger.order.entity;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.FetchType.EAGER;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.json.Json;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Length;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "OrderTable", uniqueConstraints = {
        @UniqueConstraint(name = "Order_orderer_unique", columnNames = {Order.Fields.orderer}),
        @UniqueConstraint(name = "Order_configuration_unique", columnNames = {Order.Fields.configuration})
})
@Entity
public class Order
{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_generator")
    @SequenceGenerator(name = "order_generator", sequenceName = "order_sequence")
    private Long                 id;

    @NotNull(message = "{Order.orderer.NotNull}")
    @Length(min = 3, max = 100, message = "{Order.orderer.Length}")
    private String               orderer;

    @NotNull(message = "{Order.visible.NotNull}")
    private Boolean              visible = false;

    @Length(max = 512, message = "{Order.comment.Length}")
    private String               comment;

    @Valid
    @Size(min = 6, max = 6)
    @OneToMany(fetch = EAGER, orphanRemoval = true, cascade = ALL, mappedBy = ImageReference.Fields.order)
    private List<ImageReference> images;

    private String               configuration;

    /**
     * Workaround setter for back reference on {@link ImageReference#order}
     */
    public Order setImages(List<ImageReference> images)
    {
        TreeSet<Long> configs = new TreeSet<>();
        List<ImageReference> copy = new ArrayList<>();
        for (ImageReference imageReference : images) {
            ImageReference copiedReference = imageReference;
            copiedReference.setOrder(this);
            copy.add(copiedReference);
            configs.add(imageReference.getImage().getId());
        }
        this.images = copy;
        configuration = Json.createArrayBuilder(configs).build().toString();
        return this;
    }
}
