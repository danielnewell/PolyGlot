/*
 * Copyright (c) 2019, draque
 * All rights reserved.
 *
 * Licensed under: Creative Commons Attribution-NonCommercial 4.0 International Public License
 *  See LICENSE.TXT included with this code to read the full license agreement.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package PolyGlot.CustomControls;

import PolyGlot.DictCore;
import PolyGlot.ManagersCollections.ConWordCollection.ConWordDisplay;
import PolyGlot.Nodes.ConWord;

/**
 * This specifically handles list cases for the Lexicon display
 * @author draque
 */
public class PListLexicon extends PList<ConWordDisplay> {
    public PListLexicon(DictCore _core, boolean _isConFont) {        
        super(_core, _isConFont);
    }
    
    @Override
    public void setSelectedValue(Object value, boolean shouldScroll) {
        Object passThrough = value;
        
        if (value instanceof ConWord && this.getModel() instanceof PListModelLexicon) {
            passThrough = ((PListModelLexicon)this.getModel()).getDisplayFromWord((ConWord)value);
            passThrough = passThrough == null ? value : passThrough;
        }
        
        super.setSelectedValue(passThrough, shouldScroll);
    }
}
