package com.tugmamat.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

/** Friendly message when an uploaded image is too large. */
@ControllerAdvice
public class ErrorAdvice {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView tooLarge() {
        ModelAndView mv = new ModelAndView("error");
        mv.addObject("title", "Image is too large");
        mv.addObject("message", "Images can be 5MB at most. Choose a smaller file and try again.");
        return mv;
    }
}
