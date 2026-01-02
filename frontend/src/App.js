import React, {useState} from "react";
import axios from "axios";
function App(){
  const [file,setFile] = useState(null);
  const [doc,setDoc] = useState(null);
  async function upload(e){
    e.preventDefault();
    if(!file) return;
    const fd = new FormData();
    fd.append("file", file);
    const res = await axios.post("/api/documents/upload", fd);
    setDoc(res.data);
  }
  return (<div style={{padding:20}}>
    <h3>InSIGHTPDF Upload</h3>
    <form onSubmit={upload}>
      <input type="file" onChange={e=>setFile(e.target.files[0])} />
      <button type="submit">Upload</button>
    </form>
    {doc && <div>
      <h4>Document</h4>
      <pre>{JSON.stringify(doc,null,2)}</pre>
    </div>}
  </div>);
}
export default App;
