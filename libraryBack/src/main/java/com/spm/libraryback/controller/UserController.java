package com.spm.libraryback.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spm.libraryback.LoginUser;
import com.spm.libraryback.commom.Result;
import com.spm.libraryback.entity.BookWithUser;
import com.spm.libraryback.entity.User;
import com.spm.libraryback.mapper.BookWithUserMapper;
import com.spm.libraryback.mapper.CommentMapper;
import com.spm.libraryback.mapper.UserMapper;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {
    @Resource
    UserMapper userMapper;
    @Resource
    BookWithUserMapper bookWithUserMapper;
    @Resource
    CommentMapper commentMapper;
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user){
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername,user.getUsername()));
        if(res != null)
        {
            return Result.error("-1","用户名已重复");
        }
        userMapper.insert(user);
        return Result.success();
    }
    @CrossOrigin
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user){
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername,user.getUsername()).eq(User::getPassword,user.getPassword()));
        if(res == null)
        {
            return Result.error("-1","用户名或密码错误");
        }
        LoginUser loginuser = new LoginUser();
        loginuser.addVisitCount();
        return Result.success(res);
    }
    @PostMapping
    public Result<?> save(@RequestBody User user){
        if(user.getPassword() == null){
            user.setPassword("abc123456");
        }
        userMapper.insert(user);
        return Result.success();
    }
    @PutMapping("/password")
    public  Result<?> update( @RequestParam Integer id,
                              @RequestParam String password2){
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id",id);
        User user = new User();
        user.setPassword(password2);
        userMapper.update(user,updateWrapper);
        return Result.success();
    }
    @PutMapping
    public  Result<?> password(@RequestBody User user){
        userMapper.updateById(user);
        return Result.success();
    }
    @PostMapping("/deleteBatch")
    public  Result<?> deleteBatch(@RequestBody List<Integer> ids){
        for(int id:ids)
        {
            User user = userMapper.selectById(id);
            QueryWrapper<BookWithUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("nick_name", user.getUsername());
            if (bookWithUserMapper.selectCount(queryWrapper)>0)
            {
                return Result.error("400","Someone hasn't returned the book yet");
            }
        }
        userMapper.deleteBatchIds(ids);
        return Result.success();
    }
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id){
        User user = userMapper.selectById(id);
        QueryWrapper<BookWithUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("nick_name", user.getUsername());
        if (bookWithUserMapper.selectCount(queryWrapper)>0)
        {
            return Result.error("400","This patron hasn't returned the book yet");
        }
        else
        {
            commentMapper.updateComment(user.getUsername()+"(not exist)",id);
            userMapper.deleteById(id);
            return Result.success();
        }
    }
    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search){
        LambdaQueryWrapper<User> wrappers = Wrappers.<User>lambdaQuery();
        if(StringUtils.isNotBlank(search)){
            wrappers.like(User::getNickName,search);
        }
        wrappers.like(User::getRole,2);
        Page<User> userPage =userMapper.selectPage(new Page<>(pageNum,pageSize), wrappers);
        return Result.success(userPage);
    }
    @GetMapping("/usersearch")
    public Result<?> findPage2(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search1,
                               @RequestParam(defaultValue = "") String search2,
                               @RequestParam(defaultValue = "") String search3,
                               @RequestParam(defaultValue = "") String search4){
        LambdaQueryWrapper<User> wrappers = Wrappers.<User>lambdaQuery();
        if(StringUtils.isNotBlank(search1)){
            wrappers.like(User::getId,search1);
        }
        if(StringUtils.isNotBlank(search2)){
            wrappers.like(User::getNickName,search2);
        }
        if(StringUtils.isNotBlank(search3)){
            wrappers.like(User::getPhone,search3);
        }
        if(StringUtils.isNotBlank(search4)){
            wrappers.like(User::getAddress,search4);
        }
        wrappers.like(User::getRole,2);
        Page<User> userPage =userMapper.selectPage(new Page<>(pageNum,pageSize), wrappers);
        return Result.success(userPage);
    }
}
