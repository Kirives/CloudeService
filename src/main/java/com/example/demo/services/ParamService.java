package com.example.demo.services;

import com.example.demo.dto.BreadDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ParamService {
    public List<String> getFolders(List<String> allFiles, int id){
        List<String> folderList = new ArrayList<String>();
        for(String result : allFiles) {
            List<String> mini = List.of(result.split("/"));
            if(result.charAt(result.length()-1) == '/') {
                if(!mini.get(mini.size()-1).equals("user-"+String.valueOf(id)+"-files")){
                    folderList.add(mini.get(mini.size()-1));
                }
            }
        }
        return folderList;
    }

    public List<BreadDto> getBread(String path){
        List<BreadDto> test = new ArrayList<>();
        while (path.lastIndexOf("/")!=-1){
            int index = path.lastIndexOf("/");
            path = path.substring(0,index);
            index=path.lastIndexOf("/");
            String name ="";
            if(index==-1){
                name=path;
            }else{
                name=path.substring(index+1);
            }
            test.add(BreadDto.builder().path(path).name(name).build());
        }
        test.reversed();
        return test.reversed();
    }
}
