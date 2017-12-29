package sec.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import sec.project.domain.Signup;
import sec.project.repository.SignupRepository;
//import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;

@Controller
public class SignupController {

    @Autowired
    private SignupRepository signupRepository;

    @RequestMapping("*")
    public String defaultMapping() {
        return "redirect:/form";
    }

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String loadForm() {
        return "form";
    }

    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public String submitForm(@RequestParam String name, @RequestParam String address) {
        signupRepository.save(new Signup(name, address));
        return "done";
    }
    
    @RequestMapping(value = "/remove/{id}", method = RequestMethod.POST)
    public String remove(@PathVariable Long id) {
        if(id != null)
        {
           Signup remove = signupRepository.findById(id);
           signupRepository.delete(remove);
           
        }
        System.out.println("Delete invoked");
        return "redirect:/manage";
    }
    
    @RequestMapping(value = "/removeAll", method = RequestMethod.POST)
    public String removeAll() {
        signupRepository.deleteAll();
        return "redirect:/manage";
    }
    
    @RequestMapping(value = "/manage", method = RequestMethod.GET)
    public String manageForm(Principal principal, Model model) {
        model.addAttribute("signups", signupRepository.findAll());
 
        System.out.println(principal.getName());
        if(principal.getName().equals("admin"))
        {
            return "signuplistAdmin";
        }
        else
        {
            return "signuplist";
        }
    }
    
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public String deleteConfirmation(Model model, @PathVariable Long id) {
        if (id != null)
        {
            model.addAttribute("signup", signupRepository.findById(id));
            return "deleteConfirm";
        }
        else
        {
            return"redirect:/form";
        }
    }
    @RequestMapping(value = "redirect", method = RequestMethod.GET)
    public String redirect(@RequestParam String url)
    {
        System.out.println("Redirect");
        return "redirect://" + url;
    }

}
