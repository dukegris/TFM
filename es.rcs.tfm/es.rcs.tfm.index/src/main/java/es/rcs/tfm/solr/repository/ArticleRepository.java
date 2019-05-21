package es.rcs.tfm.solr.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;

import es.rcs.tfm.solr.model.Article;

public interface ArticleRepository extends SolrCrudRepository<Article, String> {
 
	public List<Article> findByTitle(String title);
	 
	@Query( "id:*?0* OR title:*?0*" )
	public Page<Article> findByCustomQuery(String searchTerm, Pageable pageable);
	 
	@Query( name = "Article.findByNamedQuery" )
	public Page<Article> findByNamedQuery(String searchTerm, Pageable pageable);

}
