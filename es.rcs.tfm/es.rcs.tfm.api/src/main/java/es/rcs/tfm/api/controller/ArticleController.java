package es.rcs.tfm.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.base.Preconditions;

import es.rcs.tfm.api.ApiNames;
import es.rcs.tfm.solr.model.PubArticleIdx;
import es.rcs.tfm.solr.repository.PubArticleIdxRepository;

@Controller
@RequestMapping(ApiNames.API_URL_BASE + ApiNames.API_ARTICLES)
public class ArticleController {

    @Autowired
    private PubArticleIdxRepository repository;

	public static class RestPreconditions {
	    public static <T> T checkFound(T resource) throws Exception {
	        if (resource == null) {
	            throw new Exception("Not found");
	        }
	        return resource;
	    }
	}
	
	@GetMapping
	public Iterable<PubArticleIdx> findAll() {
		Iterable<PubArticleIdx> instances = repository.findAll();
		return instances;
	}

	@GetMapping(value = "/{id}")
	public PubArticleIdx findById(@PathVariable("id") String id) throws Exception {
		PubArticleIdx instance = RestPreconditions.checkFound(repository.findById(id).get());
		return instance;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PubArticleIdx create(@RequestBody PubArticleIdx resource) {
		Preconditions.checkNotNull(resource);
		PubArticleIdx instance = repository.save(resource);
		return instance;
	}

	@PutMapping(value = "/{id}")
	@ResponseStatus(HttpStatus.OK)
	public void update(@PathVariable("id") String id, @RequestBody PubArticleIdx resource) {
		Preconditions.checkNotNull(resource);
		Preconditions.checkNotNull(repository.findById(resource.getId()).get());
		repository.save(resource);
	}

	@DeleteMapping(value = "/{id}")
	@ResponseStatus(HttpStatus.OK)
	public void delete(@PathVariable("id") String id) {
		repository.deleteById(id);
	}
	
}
