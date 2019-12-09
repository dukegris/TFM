package es.rcs.tfm.srv.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper=false)
public class ArticuloTextoAnotado {

	private String pmid = "";
	private List<BloqueAnotado> blocks = new ArrayList<BloqueAnotado>();

	public void addBlocks(List<BloqueAnotado> items) {
		if ((items!= null) && (!items.isEmpty())) this.blocks.addAll(items);
	}

	public void addBlock(BloqueAnotado value) {
		if (value != null) blocks.add(value);
	}

	public boolean containsBlockOfType(String type) {
		if (StringUtils.isBlank(type)) return false;
		boolean result = false;
		for (BloqueAnotado block: this.blocks) {
			if (type.equals(block.getType())) {
				result = true;
				break;
			}
		}
		return result;
	}

	public BloqueAnotado getBlocksOfType(String type) {
		if (StringUtils.isBlank(type)) return null;
		BloqueAnotado result = null;
		for (BloqueAnotado block: this.blocks) {
			if (type.equals(block.getType())) {
				result = block;
				break;
			}
		}
		return result;
	}

}
