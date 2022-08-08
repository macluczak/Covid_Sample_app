# Covid_Sample_app

## Covid recruitment application
--------------------------

Recruitment task, errors related to downloading and creating files in external data storage have been found and eliminated in the application


The application creates a separate /inkbook folder in external data storage, the introduced improvement is the lack of redundant downloading of files.
the application downloads one specific file, each subsequent download overwrites the current file so as not to take up excess space in memory. 
Another improvement introduced is that the applications find an already downloaded file at the start

any blocking I/O operations have been implemented inside coroutines with Disptcher.IO

As part of the introduction of clean code, the implementation of a mechanism to prevent the loss of states during screen rotations and the provision of SSoT (Single Source of Truth), applications were implemented within the mvvm architecture pattern


<table>
  <tr>
    <td> <img src="https://user-images.githubusercontent.com/77066408/183403437-83844d3c-a455-4982-a1bb-cf56bb4580d9.png"  alt="1" ></td>
    <td> <img src="https://user-images.githubusercontent.com/77066408/183403531-8aae4715-6959-4131-b58e-eee41a3866b2.png"  alt="2" ></td>
    <td> <img src="https://user-images.githubusercontent.com/77066408/183403931-6641c3a8-96f2-45ba-b2cb-9e3fb0dc2a8d.png"  alt="3" ></td>
     
  </tr> 
</table>

