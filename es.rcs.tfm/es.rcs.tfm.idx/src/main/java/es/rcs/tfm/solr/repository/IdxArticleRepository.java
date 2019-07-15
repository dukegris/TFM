package es.rcs.tfm.solr.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;

import es.rcs.tfm.solr.IndexNames;
import es.rcs.tfm.solr.model.IdxArticleSolr;

@Repository(IndexNames.IDX_ALL_ARTICLES_REP)
public interface IdxArticleRepository extends 
		SolrCrudRepository<IdxArticleSolr, String> {
	 
	public List<IdxArticleSolr> findByTitle(
			String title);
	 
	public List<IdxArticleSolr> findByPmid(
			String pmid);
	 
	@Query( "id:*?0* OR title:*?0*" )
	public Page<IdxArticleSolr> findByCustomQuery(
			String searchTerm, 
			Pageable pageable);
	 
	@Query( name = "Article.findInSummary" )
	public Page<IdxArticleSolr> findInSummary(
			String searchTerm, 
			Pageable pageable);

}
