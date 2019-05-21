package es.rcs.tfm.web.controller;

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

import es.rcs.tfm.solr.model.Article;
import es.rcs.tfm.solr.repository.ArticleRepository;

@Controller
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private ArticleRepository repository;

	public static class RestPreconditions {
	    public static <T> T checkFound(T resource) throws Exception {
	        if (resource == null) {
	            throw new Exception("Not found");
	        }
	        return resource;
	    }
	}
	
	@GetMapping
	public Iterable<Article> findAll() {
		Iterable<Article> instances = repository.findAll();
		return instances;
	}

	@GetMapping(value = "/{id}")
	public Article findById(@PathVariable("id") String id) throws Exception {
		Article instance = RestPreconditions.checkFound(repository.findById(id).get());
		return instance;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Article create(@RequestBody Article resource) {
		Preconditions.checkNotNull(resource);
		Article instance = repository.save(resource);
		return instance;
	}

	@PutMapping(value = "/{id}")
	@ResponseStatus(HttpStatus.OK)
	public void update(@PathVariable("id") String id, @RequestBody Article resource) {
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
