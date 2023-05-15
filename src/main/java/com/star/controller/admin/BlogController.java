package com.star.controller.admin;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.star.entity.Blog;
import com.star.entity.Type;
import com.star.entity.User;
import com.star.queryvo.BlogQuery;
import com.star.queryvo.SearchBlog;
import com.star.queryvo.ShowBlog;
import com.star.service.BlogService;
import com.star.service.TypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

/**
 * @Description: 博客管理控制器
 */
@Controller
@RequestMapping("/admin")
public class BlogController {

    @Autowired
    private BlogService blogService;
    @Autowired
    private TypeService typeService;

    //跳转博客新增页面，匹配Get请求
//    通过getAllType()方法获取所有博客类型，然后将其添加到Model对象中，以便在前端页面中显示。
//    创建一个Blog对象，并将其也添加到Model对象中。这个Blog对象会在前端页面中被绑定到表单上，用户可以通过填写表单来创建新博客。
    @GetMapping("/blogs/input")
    public String input(Model model) {
        model.addAttribute("types",typeService.getAllType());
        model.addAttribute("blog", new Blog());
        return "admin/blogs-input";
    }

    //博客新增，匹配POST请求
    @PostMapping("/blogs")
    public String post(Blog blog, RedirectAttributes attributes, HttpSession session){
        //新增的时候需要传递blog对象，blog对象需要有user
        blog.setUser((User) session.getAttribute("user"));
        //设置blog的type
        blog.setType(typeService.getType(blog.getType().getId()));
        //设置blog中typeId属性
        blog.setTypeId(blog.getType().getId());
        //设置用户id
        blog.setUserId(blog.getUser().getId());

        int b = blogService.saveBlog(blog);
        if(b == 0){
            attributes.addFlashAttribute("message", "新增失败");
        }else {
            attributes.addFlashAttribute("message", "新增成功");
        }
        return "redirect:/admin/blogs";
    }

    //博客列表
    @RequestMapping("/blogs")
    public String blogs(Model model, @RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum){
        //指定排序字段为“update_time”，按照倒序排列
        String orderBy = "update_time desc";
//      用来启动MyBatis分页插件，设置每页显示10条记录，按照orderBy所指定的方式进行排序，pageNum表示当前页码数，默认值为1。
        PageHelper.startPage(pageNum,10,orderBy);
//      获取所有博客记录的信息
        List<BlogQuery> list = blogService.getAllBlog();
//      通过PageInfo对象包装查询结果，其中包含了详细的分页信息
        PageInfo<BlogQuery> pageInfo = new PageInfo<BlogQuery>(list);
//      将所有的博客类型信息传递到前端页面
        model.addAttribute("types",typeService.getAllType());
//        将分页数据传递到前端页面
        model.addAttribute("pageInfo",pageInfo);
        return "admin/blogs";
    }

    //删除博客
    @GetMapping("/blogs/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes attributes) {
        blogService.deleteBlog(id);
//        将一个名为"message"的Flash属性添加到重定向请求中，以便重定向后能够访问该属性。在本例中，属性值为"删除成功"。
        attributes.addFlashAttribute("message", "删除成功");
        return "redirect:/admin/blogs";
    }

    //跳转编辑修改文章
    @GetMapping("/blogs/{id}/input")
    public String editInput(@PathVariable Long id, Model model) {
        ShowBlog blogById = blogService.getBlogById(id);
        List<Type> allType = typeService.getAllType();
        model.addAttribute("blog", blogById);
        model.addAttribute("types", allType);
        return "admin/blogs-input";
    }

    //编辑修改文章
    @PostMapping("/blogs/{id}")
    public String editPost(@Valid ShowBlog showBlog, RedirectAttributes attributes) {
        int b = blogService.updateBlog(showBlog);
        if(b == 0){
            attributes.addFlashAttribute("message", "修改失败");
        }else {
            attributes.addFlashAttribute("message", "修改成功");
        }
        return "redirect:/admin/blogs";
    }

    //搜索博客管理列表
    @PostMapping("/blogs/search")
    public String search(SearchBlog searchBlog, Model model,
                         @RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum) {
        List<BlogQuery> blogBySearch = blogService.getBlogBySearch(searchBlog);
//        使用PageHelper插件对blogBySearch进行分页处理，并将结果存入PageInfo<BlogQuery>类型的变量pageInfo中
        PageHelper.startPage(pageNum, 10);
        PageInfo<BlogQuery> pageInfo = new PageInfo<>(blogBySearch);
//        将pageInfo作为属性添加到model中
        model.addAttribute("pageInfo", pageInfo);
        return "admin/blogs :: blogList";
    }

}