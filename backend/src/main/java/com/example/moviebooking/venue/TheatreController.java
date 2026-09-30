package com.example.moviebooking.venue;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/v1/theatres")
public class TheatreController {
 private final TheatreService service;
 public TheatreController(TheatreService service){this.service=service;}
 @GetMapping public Page<Theatre> list(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.list(page,size);}
 @GetMapping("/{id}") public Theatre get(@PathVariable String id){return service.get(id);}
 @PostMapping public Theatre create(@Valid @RequestBody Theatre theatre){return service.create(theatre);}
 @PutMapping("/{id}") public Theatre update(@PathVariable String id,@Valid @RequestBody Theatre theatre){return service.update(id,theatre);}
 @DeleteMapping("/{id}") public void delete(@PathVariable String id){service.delete(id);}
 @GetMapping("/{id}/screens") public List<Screen> screens(@PathVariable String id){return service.screens(id);}
 @PostMapping("/{id}/screens") public Screen createScreen(@PathVariable String id,@RequestBody Screen screen){return service.createScreen(id,screen);}
 @PutMapping("/screens/{screenId}") public Screen updateScreen(@PathVariable String screenId,@RequestBody Screen screen){return service.updateScreen(screenId,screen);}
}
